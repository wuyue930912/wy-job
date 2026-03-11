package com.ts.service;

import com.ts.annotation.TsJOB;
import com.ts.config.TsJobConfig;
import com.ts.dto.JobDTO;
import com.ts.mapper.TsJobRecordDAO;
import com.ts.po.TsJobRecordPO;
import com.ts.provider.TsJobYmlProvider;
import com.ts.util.TsJobSpringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 任务调度服务
 * 支持失败重试和超时控制
 * 
 * @author yue.wu
 * @description 任务调度
 * @date 2023/5/5 13:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TsJobRecordDAO tsJobRecordDAO;

    private final TsJobYmlProvider tsJobYmlProvider;

    /**
     * 任务执行线程池
     */
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    public void runTaskByQuartzKey(JobKey key) {
        String jobKey = key.getName();
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        
        if (Boolean.TRUE.equals(tsJobYmlProvider.ymlConfig().get("enableRecord"))) {
            if (Objects.nonNull(job)) {
                // 记录执行记录，状态执行中
                TsJobRecordPO po = new TsJobRecordPO();
                po.setRecordStatus(3);
                po.setJobKey(job.getKey());
                tsJobRecordDAO.insert(po);

                // 执行任务（带重试和超时）
                boolean success = executeWithRetryAndTimeout(job, po);
                
                po.setEndTime(new Timestamp(System.currentTimeMillis()));
                po.setRecordStatus(success ? 1 : 2);
                tsJobRecordDAO.updateById(po);
            }
        }
    }

    /**
     * 执行任务，支持重试和超时
     */
    private boolean executeWithRetryAndTimeout(JobDTO job, TsJobRecordPO record) {
        int maxRetries = job.getRetryCount();
        int timeoutSeconds = job.getTimeout();
        long retryInterval = job.getRetryInterval();
        
        // 获取注解配置（如果 JobDTO 没有则从注解读取）
        if (maxRetries == 0 && timeoutSeconds == 0) {
            TsJOB annotation = job.getaMethod().getAnnotation(TsJOB.class);
            if (annotation != null) {
                maxRetries = annotation.retryCount();
                timeoutSeconds = annotation.timeout();
                retryInterval = annotation.retryInterval();
            }
        }

        Exception lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                // 重试前等待
                log.info("[ts-job] Job [{}] retry {}/{} after {}ms", 
                        job.getKey(), attempt, maxRetries, retryInterval);
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 执行任务
            if (timeoutSeconds > 0) {
                // 有超时控制
                success = executeWithTimeout(job, timeoutSeconds);
            } else {
                // 无超时控制
                success = executeJob(job);
            }
            
            if (success) {
                log.info("[ts-job] Job [{}] succeeded on attempt {}", job.getKey(), attempt + 1);
                return true;
            }
            
            lastException = new Exception("Task execution failed");
        }
        
        log.error("[ts-job] Job [{}] failed after {} attempts", job.getKey(), maxRetries + 1);
        return false;
    }

    /**
     * 带超时执行任务
     */
    private boolean executeWithTimeout(JobDTO job, int timeoutSeconds) {
        boolean success;
        try {
            Future<?> future = taskExecutor.submit(() -> executeJob(job));
            success = future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("[ts-job] Job [{}] timeout after {}s", job.getKey(), timeoutSeconds);
            return false;
        } catch (Exception e) {
            log.error("[ts-job] Job [{}] execution error: {}", job.getKey(), e.getMessage());
            return false;
        }
        return success;
    }
        Future<?> future = taskExecutor.submit(() -> executeJob(job));
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("[ts-job] Job [{}] timeout after {}s", job.getKey(), timeoutSeconds);
            future.cancel(true);
            return false;
        } catch (Exception e) {
            log.error("[ts-job] Job [{}] execution error: {}", job.getKey(), e.getMessage());
            return false;
        }
    }

    /**
     * 执行单个任务
     */
    private boolean executeJob(JobDTO job) {
        try {
            Object service = TsJobSpringUtils.getBean(job.getClassName());
            Method method = ReflectionUtils.findMethod(service.getClass(), job.getaMethod().getName());
            if (method == null) {
                log.error("[ts-job] Method not found: {}.{}", job.getClassName(), job.getaMethod().getName());
                return false;
            }
            ReflectionUtils.invokeMethod(method, service);
            return true;
        } catch (Exception e) {
            log.warn("[ts-job] Job [{}] execution failed: {}", job.getKey(), e.getMessage());
            return false;
        }
    }
    
