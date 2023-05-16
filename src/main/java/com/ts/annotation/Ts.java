package com.ts.annotation;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author yue.wu
 * @description TODO
 * @date 2022/4/1 11:48
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Ts {

}
