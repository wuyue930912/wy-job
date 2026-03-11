# wy-job

> 一个轻量级的调度工具，自带简易页面，配置方便。可以动态配置调度频率，随时启动、关闭调度，并记录执行状态。
> ### 源码地址： https://github.com/wuyue930912/wy-job

-------------------
## 1、引入依赖

```java
<dependency>
  <groupId>org.wy</groupId>
  <artifactId>ts-base-job-starter</artifactId>
  <version>1.1.12</version>
</dependency>
```

----------------
## 2、创建表
### postgres

```sql
-------------------------------------------------------------------------

create table public.ts_job
(
  id          varchar   not null,
  job_name    varchar   not null,
  job_des     varchar   null,
  job_key     varchar   not null,
  cron        varchar   not null,
  create_time timestamp not null default CURRENT_TIMESTAMP,
  create_user varchar   null,
  version     int4      null
);

create unique index "u_idx_job_name" on
    "ts_job"
        using btree ("job_name");

create unique index "u_idx_key" on
    "ts_job"
        using btree ("job_key");


create table public.ts_job_record
(
  id            varchar   not null,
  job_key       varchar   not null,
  record_time   timestamp not null default CURRENT_TIMESTAMP,
  end_time      timestamp null,
  record_status int4      not null default 3
);

-------------------------------------------------------------------------

```

### mysql

```sql

create table ts_job
(
    id          varchar(64)  not null comment '主键(UUID)',
    job_name    varchar(64)  null comment '用户名',
    job_des     varchar(64)  null comment '角色类型',
    job_key     varchar(50)  null comment '登陆账号',
    cron        varchar(128) null comment '登录密码(BCrypt)',
    create_time timestamp    null comment '创建时间',
    create_user varchar(64)  null comment '创建人',
    version     int(4) null comment 'LOCK',
    constraint ts_job
        primary key (id)
);

create unique index u_idx_job_name on ts_job (job_name);
create unique index u_idx_key on ts_job (job_key);

CREATE TABLE ts_job_record
(
    id            varchar(64) NOT NULL,
    job_key       varchar(64) NOT NULL,
    record_time   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time      timestamp   null,
    record_status int(4) NOT NULL DEFAULT 3,
    constraint ts_job_record
        primary key (id)
);
```

-----------------

## 3、使用注解标记类和方法
![在这里插入图片描述](https://img-blog.csdnimg.cn/21758f6ca6ba4fd8bb272433870c9c98.png)

------------
## 4、启动项目
### 浏览器访问：
http://{ip}:{port}/ts-job/index.html
#### 其中key填写代码中@TsJOB注解中的key
![在这里插入图片描述](https://img-blog.csdnimg.cn/06a8e1e441ee4be8b82b6d01d9f83071.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/745b63b668c84a7586aaca00d5e85c2b.png)
![在这里插入图片描述](https://img-blog.csdnimg.cn/cab76ac7ddf8431184bf26ea10070210.png)

---------------

## 5、常见问题

1、依赖冲突直接排除冲突依赖即可

2、如果使用了mybatis-plus必须要3.4.1及以上版本

3、仅支持JDK8及以上版本

4、访问不到页面检查Security配置或者SpringMvcConfig配置

-----------------
## 6、若发现其他问题可以在这里反馈，万分感谢！

-----------------

## 7、配置说明

### 基础配置 (application.yml)

```yaml
ts-job:
  # 是否显示启动Banner
  enable-banner: true
  # 是否开启执行记录
  enable-record: true
  # 登录配置
  login:
    # 是否启用登录验证 (默认启用)
    enabled: true
    # 用户名 (默认 admin)
    username: admin
    # 密码 (默认 admin123)
    password: admin123
```

### 登录功能

从 v1.2.67 开始，系统默认启用登录验证。首次登录：
- 用户名：`admin`
- 密码：`admin123`

建议首次登录后修改密码。

如需禁用登录：
```yaml
ts-job:
  login:
    enabled: false
```

-----------------

## 8、新功能说明 (v1.1.12+)

### 8.1 任务并发控制

支持配置任务的最大并发数，避免同一任务同时执行：

```java
@TsJOB(
    key = "myJob",
    description = "我的任务",
    maxConcurrent = 1  // 最多同时运行1个实例
)
public void execute() {
    // 任务逻辑
}
```

### 8.2 任务依赖

支持配置任务依赖，确保依赖任务执行成功后再执行当前任务：

```java
@TsJOB(
    key = "dependencyJob",
    description = "依赖任务",
    dependencies = {"parentJob1", "parentJob2"},  // 依赖的任务key
    dependencyCheckSuccess = true  // 是否检查依赖任务执行成功
)
public void execute() {
    // 任务逻辑
}
```

### 8.3 慢任务告警

支持配置慢任务阈值，超过阈值自动告警：

```java
@TsJOB(
    key = "slowJob",
    description = "慢任务",
    slowThreshold = 30  // 超过30秒视为慢任务，触发告警
)
public void execute() {
    // 任务逻辑
}
```

### 8.4 执行统计API

新增以下API用于获取执行统计：

- `GET /ts-job/get-all-stats` - 获取所有任务的执行统计
- `GET /ts-job/get-job-stats?jobKey=xxx` - 获取指定任务的执行统计
- `GET /ts-job/get-running-jobs` - 获取正在运行的任务列表
- `GET /ts-job/get-registered-jobs` - 获取所有已注册的任务列表
- `POST /ts-job/clear-stats?jobKey=xxx` - 清除任务统计

-----------------

## 更新记录

### 2026-03-11 (第三版)
- **配置优化**
  - 新增告警配置（Alert）类，支持Webhook、企业微信告警
  - 新增调度配置（Scheduler）类，支持默认超时、重试、并发等配置
- **代码优化**
  - TaskService 增加详细的执行日志和异常处理
  - 新增 `executeWithTimeoutDetailed` 和 `executeJobDetailed` 方法，提供更详细的执行结果
  - 优化重试机制，增加每次尝试的详细日志
  - 添加任务超时告警触发逻辑
  - 完善异常捕获，避免告警发送失败影响主流程
- **API增强**
  - 新增 `/ts-job/get-today-stats` - 获取今日执行统计
  - 新增 `/ts-job/get-recent-failed` - 获取近期失败记录
  - 新增 `/ts-job/get-slow-records` - 获取慢任务记录
  - 新增 `/ts-job/get-job-detail-stats` - 获取任务详细统计
- **DAO层扩展**
  - 新增 `selectTodayRecords` - 查询今日执行记录
  - 新增 `selectRecentFailedRecords` - 查询近期失败记录
  - 新增 `selectTodayRecordBI` - 统计今日各状态执行数量
  - 新增 `selectSlowRecords` - 查询慢任务记录
  - 新增 `selectTopJobExecutions` - 统计执行次数Top任务
  - 新增 `selectJobStatsByTimeRange` - 获取时间范围内统计
- **日志完善**
  - 增加任务执行各阶段的详细日志
  - 添加任务启动、执行、成功、失败、耗时等关键节点日志
  - 优化异常日志输出格式

### 2026-03-11 (第二版)
- 新增任务暂停/恢复功能（TaskSuspendService）
  - `GET /ts-job/suspend-job?jobKey=xxx` - 暂停任务
  - `GET /ts-job/resume-job?jobKey=xxx` - 恢复任务
  - `GET /ts-job/get-suspended-jobs` - 获取暂停任务列表
- 新增调度健康检查服务（SchedulerHealthService）
  - `GET /ts-job/health` - 获取调度健康状态
  - `GET /ts-job/summary` - 获取调度摘要信息
- 新增任务执行历史查询API
  - `GET /ts-job/get-job-records?jobKey=xxx&limit=10` - 获取任务执行历史
- 优化任务执行检查：暂停的任务不会被调度执行
- 任务删除时自动清除暂停状态
- 完善日志输出和异常处理

### 2026-03-11
- 新增任务并发控制功能（maxConcurrent参数）
- 新增任务依赖功能（dependencies参数）
- 新增任务慢查询告警功能（slowThreshold参数）
- 新增任务执行统计功能，包含成功率、平均耗时等
- 新增任务告警服务接口（JobAlertService）和默认实现
- 修复TaskService中重复代码的编译错误
- 完善异常处理和日志记录
- 新增多个API端点用于查询执行统计和运行状态
- 优化任务执行流程，添加完整的try-catch保护

### 2026-01-01
- 新增登录认证功能
- 优化页面UI