package com.ts.dto;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务数据传输对象
 * 包含任务的所有配置信息
 * 
 * @author wy-job
 */
@Data
public class JobDTO {

    /**
     * 任务唯一标识
     */
    private String key;
    
    /**
     * 任务类
     */
    private Class aClass;
    
    /**
     * 任务类名称（首字母小写）
     */
    private String className;
    
    /**
     * 任务方法
     */
    private Method aMethod;
    
    /**
     * 任务描述
     */
    private String description;
    
    // ==================== 原有配置 ====================
    
    /**
     * 失败重试次数
     */
    private int retryCount;
    
    /**
     * 任务超时时间（秒）
     */
    private int timeout;
    
    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval;

    // ==================== 新增配置：并发控制 ====================
    
    /**
     * 最大并发数（0或不设置表示无限制）
     */
    private int maxConcurrent = 0;

    // ==================== 新增配置：任务依赖 ====================
    
    /**
     * 依赖的任务Key列表（这些任务执行成功后，当前任务才会执行）
     */
    private List<String> dependencies = new ArrayList<>();
    
    /**
     * 是否检查依赖任务执行成功（true: 依赖任务最近执行必须成功）
     */
    private boolean dependencyCheckSuccess = true;

    // ==================== 新增配置：慢查询告警 ====================
    
    /**
     * 慢任务阈值（秒），超过此时间执行则触发告警（0表示不启用）
     */
    private int slowThreshold = 0;

    // ==================== 新增配置：执行统计 ====================
    
    /**
     * 启用执行统计
     */
    private boolean enableStats = true;

    @Override
    public String toString() {
        return "JobDTO{" +
                "key='" + key + '\'' +
                ", description='" + description + '\'' +
                ", retryCount=" + retryCount +
                ", timeout=" + timeout +
                ", retryInterval=" + retryInterval +
                ", maxConcurrent=" + maxConcurrent +
                ", dependencies=" + dependencies +
                ", slowThreshold=" + slowThreshold +
                '}';
    }
}