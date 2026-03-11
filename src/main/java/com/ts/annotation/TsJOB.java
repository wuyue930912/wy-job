package com.ts.annotation;

import java.lang.annotation.*;

/**
 * 定时任务注解
 * 
 * @author yue.wu
 * @description 标记定时任务方法，支持配置重试次数和超时时间
 * @date 2022/4/1 11:48
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TsJOB {
    // 调度任务KEY（唯一标识）
    String key();

    // 调度描述
    String description() default "ts job";
    
    // 失败重试次数（默认0，不重试）
    int retryCount() default 0;
    
    // 任务超时时间（单位：秒，0表示不限制）
    int timeout() default 0;
    
    // 重试间隔（单位：毫秒，默认1000ms）
    long retryInterval() default 1000;

    // ==================== 新增配置：并发控制 ====================
    
    /**
     * 最大并发数（默认0，表示无限制）
     * 同一个任务同时运行的最大实例数
     */
    int maxConcurrent() default 0;

    // ==================== 新增配置：任务依赖 ====================
    
    /**
     * 依赖的任务Key列表
     * 当前任务会在这些任务执行成功后才执行
     */
    String[] dependencies() default {};
    
    /**
     * 是否检查依赖任务执行成功（默认true）
     * true: 依赖任务最近一次执行必须成功
     * false: 只要依赖任务不在运行中即可
     */
    boolean dependencyCheckSuccess() default true;

    // ==================== 新增配置：慢查询告警 ====================
    
    /**
     * 慢任务阈值（单位：秒，0表示不启用）
     * 任务执行超过此时间则触发告警
     */
    int slowThreshold() default 0;
    
    // ==================== 新增配置：任务分组 ====================
    
    /**
     * 任务分组（用于任务分类管理）
     */
    String jobGroup() default "";
    
    /**
     * 任务标签（多个用逗号分隔，如: "important,daily"）
     */
    String tags() default "";
    
    // ==================== 新增配置：失败策略 ====================
    
    /**
     * 失败策略：1-停止调度 2-继续重试 3-忽略继续
     * 默认：2（继续重试）
     */
    int failStrategy() default 2;
}
