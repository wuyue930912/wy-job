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

    // ==================== 新增：执行统计字段 ====================
    
    /**
     * 总执行次数
     */
    private Long totalCount;
    
    /**
     * 执行成功率
     */
    private Double successRate;
    
    /**
     * 平均执行时间（秒）
     */
    private Double avgDuration;
    
    /**
     * 最后执行状态：1=成功, 2=失败, 3=执行中, 4=跳过
     */
    private Integer lastStatus;
    
    /**
     * 最后执行时间
     */
    private Timestamp lastExecuteTime;
}