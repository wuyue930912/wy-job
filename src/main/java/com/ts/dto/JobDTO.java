package com.ts.dto;

import java.lang.reflect.Method;

public class JobDTO {

    private String key;
    private Class aClass;
    private String className;
    private Method aMethod;
    private String description;
    private int retryCount;
    private int timeout;
    private long retryInterval;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    @Override
    public String toString() {
        return "JobDTO{" +
                "key='" + key + '\'' +
                ", aClass=" + aClass +
                ", aMethod=" + aMethod +
                ", description='" + description + '\'' +
                ", retryCount=" + retryCount +
                ", timeout=" + timeout +
                ", retryInterval=" + retryInterval +
                '}';
    }
}
