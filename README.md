# wy-job

> 一个轻量级的调度工具，自带简易页面，配置方便。可以动态配置调度频率，随时启动、关闭调度，并记录执行状态。
> ### 源码地址： https://github.com/wuyue930912/wy-job

-------------------
## 1、引入依赖

```java
<dependency>
  <groupId>org.wy</groupId>
  <artifactId>ts-base-job-starter</artifactId>
  <version>1.1.11</version>
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
