package com.ts.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@TableName("ts_job_record")
@Data
public class TsJobRecordPO {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String jobKey;
    private Timestamp recordTime;
    private Timestamp endTime;
    // 1：执行成功 2：执行失败 3：执行中
    private Integer recordStatus;

}
