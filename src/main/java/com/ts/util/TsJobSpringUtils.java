package com.ts.util;

import com.ts.annotation.Ts;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("tsJobSpringUtils")
public class TsJobSpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(String beanName) {
        if (applicationContext.containsBean(beanName)) {
            return (T) applicationContext.getBean(beanName);
        } else {
            return null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TsJobSpringUtils.applicationContext = applicationContext;
    }

    public Map<String, Object> getApplicationContext(){
        return applicationContext.getBeansWithAnnotation(Ts.class);
    }

}
