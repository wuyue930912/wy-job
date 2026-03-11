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
}
