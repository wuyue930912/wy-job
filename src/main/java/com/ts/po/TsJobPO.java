package com.ts.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@TableName("ts_job")
@Data
public class TsJobPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String jobName;
    private String jobDes;
    private String jobKey;
    private String cron;
    private Timestamp createTime;
    private String createUser;
    private Integer version;

}
