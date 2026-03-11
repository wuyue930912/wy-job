package com.ts.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    
    // ==================== 扩展字段 ====================
    
    /** 任务分组 */
    private String jobGroup;
    
    /** 任务标签（多个用逗号分隔） */
    private String tags;
    
    /** 失败策略：1-停止调度 2-继续重试 3-忽略继续 */
    private Integer failStrategy = 2;
    
    /** 是否启用：0-禁用 1-启用 */
    private Integer enabled = 1;
    
    /** 排序权重 */
    private Integer sortOrder = 0;

}
