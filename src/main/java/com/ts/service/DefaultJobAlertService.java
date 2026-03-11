package com.ts.service;

import com.ts.config.TsJobConfig;
import com.ts.dto.JobDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认任务告警服务实现
 * 支持告警频率控制、异步发送
 * 
 * @author wy-job
 */
@Slf4j
@Service
public class DefaultJobAlertService implements JobAlertService {

    /**
     * 告警配置
     */
    private final AlertConfig alertConfig;

    /**
     * 最后一次告警时间（用于频率控制）
     */
    private final ConcurrentHashMap<String, Long> lastAlertTime = new ConcurrentHashMap<>();

    /**
     * 异步执行器
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DefaultJobAlertService() {
        this(new AlertConfig());
    }

    public DefaultJobAlertService(AlertConfig config) {
        this.alertConfig = config;
        
        // 清理过期的告警记录
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            lastAlertTime.entrySet().removeIf(entry -> 
                now - entry.getValue() > alertConfig.getAlertCooldownMinutes() * 60 * 1000);
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void sendAlert(JobDTO job, String errorMessage, long duration) {
        if (!shouldAlert(job.getKey())) {
            return;
        }

        // 发送告警
        sendAlertInternal(buildAlertMessage(job, errorMessage, duration));
    }

    @Override
    public void sendTimeoutAlert(JobDTO job, long duration) {
        if (!shouldAlert(job.getKey())) {
            return;
        }

        String message = String.format("[%s] 任务执行超时！耗时: %dms", job.getKey(), duration);
        sendAlertInternal(message);
    }

    @Override
    public void sendSlowAlert(JobDTO job, long duration) {
        if (job.getSlowThreshold() <= 0) {
            return;
        }

        if (!shouldAlert(job.getKey())) {
            return;
        }

        String message = String.format("[%s] 任务执行缓慢！耗时: %dms, 阈值: %ds", 
                job.getKey(), duration, job.getSlowThreshold());
        sendAlertInternal(message);
    }

    @Override
    public boolean shouldAlert(String jobKey) {
        Long lastTime = lastAlertTime.get(jobKey);
        long now = System.currentTimeMillis();
        
        if (lastTime == null || now - lastTime > alertConfig.getAlertCooldownMinutes() * 60 * 1000) {
            lastAlertTime.put(jobKey, now);
            return true;
        }
        return false;
    }

    /**
     * 发送告警消息（可扩展为邮件、短信、Webhook等）
     */
    private void sendAlertInternal(String message) {
        log.warn("[ts-job-alert] {}", message);
        
        // 可以扩展为:
        // 1. 发送邮件
        // 2. 发送短信
        // 3. 发送Webhook通知
        // 4. 集成钉钉/企业微信
        
        if (alertConfig.isEnableWebhook() && alertConfig.getWebhookUrl() != null) {
            sendToWebhook(message);
        }
    }

    /**
     * 发送Webhook通知
     */
    private void sendToWebhook(String message) {
        try {
            // 简单的Webhook实现（可扩展）
            log.info("[ts-job-alert] Sending webhook to: {}", alertConfig.getWebhookUrl());
            // 这里可以添加实际的HTTP调用
        } catch (Exception e) {
            log.error("[ts-job-alert] Failed to send webhook: {}", e.getMessage());
        }
    }

    private String buildAlertMessage(JobDTO job, String errorMessage, long duration) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(job.getKey()).append("] 任务执行失败！");
        sb.append("\n任务名称: ").append(job.getDescription());
        sb.append("\n错误信息: ").append(errorMessage);
        sb.append("\n执行耗时: ").append(duration).append("ms");
        if (job.getRetryCount() > 0) {
            sb.append("\n重试次数: ").append(job.getRetryCount());
        }
        return sb.toString();
    }

    /**
     * 告警配置
     */
    @lombok.Data
    public static class AlertConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 是否启用Webhook
         */
        private boolean enableWebhook = false;

        /**
         * Webhook URL
         */
        private String webhookUrl;

        /**
         * 告警冷却时间（分钟）
         */
        private int alertCooldownMinutes = 30;
    }
}