package com.ts.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 
 * @author yue.wu
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/ts-job/**")
                .excludePathPatterns(
                        "/ts-job/login.html",
                        "/ts-job/api/login",
                        "/ts-job/js/**",
                        "/ts-job/css/**",
                        "/ts-job/img/**"
                );
    }
}
