package com.ts.annotation;


import java.lang.annotation.*;

/**
 * @author yue.wu
 * @description TODO
 * @date 2022/4/1 11:48
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TsJOB {
    // 调度任务KEY
    String key();

    // 调度描述
    String description() default "ts job";
}
