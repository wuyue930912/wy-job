package com.ts.vo;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class TsJobVO {

    private String id;
    private String jobName;
    private String jobDes;
    private String jobKey;
    private String cron;
    private Timestamp createTime;
    private String createUser;
    private String version;

}
