package com.ts.service;

/**
 * 任务告警服务接口
 * 支持邮件、短信、Webhook等多种告警方式
 * 
 * @author wy-job
 */
public interface JobAlertService {

    /**
     * 发送任务失败告警
     * @param job 任务信息
     * @param errorMessage 错误信息
     * @param duration 执行耗时（毫秒）
     */
    void sendAlert(JobDTO job, String errorMessage, long duration);

    /**
     * 发送任务超时告警
     * @param job 任务信息
     * @param duration 执行耗时（毫秒）
     */
    void sendTimeoutAlert(JobDTO job, long duration);

    /**
     * 发送任务执行慢告警
     * @param job 任务信息
     * @param duration 执行耗时（毫秒）
     */
    void sendSlowAlert(JobDTO job, long duration);

    /**
     * 检查告警是否需要发送（避免频繁告警）
     * @param jobKey 任务key
     * @return 是否发送
     */
    boolean shouldAlert(String jobKey);
}