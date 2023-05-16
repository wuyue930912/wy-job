-------------------------------------------------------------------------

DROP table public.ts_job;
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


drop table public.ts_job_record;
create table public.ts_job_record
(
    id            varchar   not null,
    job_key       varchar   not null,
    record_time   timestamp not null default CURRENT_TIMESTAMP,
    end_time      timestamp null,
    record_status int4      not null default 3
);

-------------------------------------------------------------------------

--- TODO 支持MYSQL数据库

DROP table if exists ts_job;
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

drop table if exists ts_job_record;
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


-- plus版
-- 线程池配置
-- key拿来做主键
-- 执行记录清理 / 执行记录开关
-- SM2 国密