package com.ts.dto;

import java.lang.reflect.Method;

public class JobDTO {

    private String key;
    private Class aClass;
    private String className;
    private Method aMethod;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Class getaClass() {
        return aClass;
    }

    public void setaClass(Class aClass) {
        this.aClass = aClass;
    }

    public Method getaMethod() {
        return aMethod;
    }

    public void setaMethod(Method aMethod) {
        this.aMethod = aMethod;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "JobDTO{" +
                "key='" + key + '\'' +
                ", aClass=" + aClass +
                ", aMethod=" + aMethod +
                '}';
    }
}
