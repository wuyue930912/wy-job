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
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 任务调度服务
 * 支持失败重试、超时控制、任务依赖、并发控制
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
     * 任务执行线程池 - 支持并发控制
     */
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

    /**
     * 正在运行的任务列表（用于并发控制）
     */
    private final ConcurrentHashMap<String, RunningJob> runningJobs = new ConcurrentHashMap<>();

    /**
     * 任务依赖检查器
     */
    private static final ConcurrentHashMap<String, Semaphore> jobSemaphores = new ConcurrentHashMap<>();

    /**
     * 执行统计信息
     */
    private static final ConcurrentHashMap<String, JobExecutionStats> jobStats = new ConcurrentHashMap<>();

    /**
     * 任务告警服务（可选）
     */
    private JobAlertService jobAlertService;

    public void setJobAlertService(JobAlertService alertService) {
        this.jobAlertService = alertService;
    }

    public void runTaskByQuartzKey(JobKey key) {
        runTaskByQuartzKey(key, null);
    }

    public void runTaskByQuartzKey(JobKey key, JobExecutionContext context) {
        String jobKey = key.getName();
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        
        if (Objects.isNull(job)) {
            log.warn("[ts-job] Job [{}] not found in registered jobs", jobKey);
            return;
        }

        // 并发控制检查
        if (!checkConcurrencyControl(job)) {
            log.warn("[ts-job] Job [{}] skipped due to concurrency control (max concurrent reached)", jobKey);
            recordJobExecution(job, 4, "concurrency control skip"); // 4 = 跳过
            return;
        }

        // 任务依赖检查
        if (!checkDependency(job)) {
            log.warn("[ts-job] Job [{}] skipped due to dependency not satisfied", jobKey);
            recordJobExecution(job, 4, "dependency not satisfied");
            return;
        }

        try {
            // 记录开始执行
            RunningJob runningJob = new RunningJob();
            runningJob.setStartTime(System.currentTimeMillis());
            runningJobs.put(jobKey, runningJob);

            // 初始化统计
            JobExecutionStats stats = jobStats.computeIfAbsent(jobKey, k -> new JobExecutionStats());

            if (Boolean.TRUE.equals(tsJobYmlProvider.ymlConfig().get("enableRecord"))) {
                // 记录执行记录，状态执行中
                TsJobRecordPO po = new TsJobRecordPO();
                po.setRecordStatus(3);
                po.setJobKey(job.getKey());
                tsJobRecordDAO.insert(po);
                runningJob.setRecordId(po.getId());

                // 执行任务（带重试和超时）
                long startTime = System.currentTimeMillis();
                ExecuteResult result = executeWithRetryAndTimeout(job, po);
                long duration = System.currentTimeMillis() - startTime;
                
                // 更新执行记录
                po.setEndTime(new Timestamp(System.currentTimeMillis()));
                po.setRecordStatus(result.isSuccess() ? 1 : 2);
                tsJobRecordDAO.updateById(po);

                // 更新统计
                stats.recordExecution(result.isSuccess(), duration);

                // 任务告警
                if (!result.isSuccess() && jobAlertService != null) {
                    jobAlertService.sendAlert(job, result.getErrorMessage(), duration);
                }
            } else {
                // 不记录时也执行任务
                executeWithRetryAndTimeout(job, null);
            }

        } catch (Exception e) {
            log.error("[ts-job] Job [{}] execution error: {}", jobKey, e.getMessage(), e);
        } finally {
            runningJobs.remove(jobKey);
            // 释放信号量
            Semaphore semaphore = jobSemaphores.get(jobKey);
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }

    /**
     * 检查并发控制
     */
    private boolean checkConcurrencyControl(JobDTO job) {
        int maxConcurrent = job.getMaxConcurrent();
        if (maxConcurrent <= 0) {
            return true; // 无限制
        }

        // 检查当前运行数量
        long currentRunning = runningJobs.entrySet().stream()
                .filter(e -> e.getKey().equals(job.getKey()))
                .count();

        if (currentRunning >= maxConcurrent) {
            return false;
        }

        // 使用信号量进行全局并发控制
        Semaphore semaphore = jobSemaphores.computeIfAbsent(job.getKey(), 
            k -> new Semaphore(maxConcurrent));
        
        return semaphore.tryAcquire();
    }

    /**
     * 检查任务依赖
     */
    private boolean checkDependency(JobDTO job) {
        List<String> dependencies = job.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }

        for (String depKey : dependencies) {
            // 检查依赖任务是否正在运行
            if (runningJobs.containsKey(depKey)) {
                return false;
            }

            // 检查依赖任务最近一次执行是否成功（可选）
            if (job.isDependencyCheckSuccess()) {
                TsJobRecordPO lastRecord = tsJobRecordDAO.selectLastByJobKey(depKey);
                if (lastRecord == null || lastRecord.getRecordStatus() != 1) {
                    log.warn("[ts-job] Job [{}] dependency [{}] last execution not successful", 
                            job.getKey(), depKey);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取执行统计信息
     */
    public JobExecutionStats getJobStats(String jobKey) {
        return jobStats.get(jobKey);
    }

    /**
     * 获取所有任务的执行统计
     */
    public Map<String, JobExecutionStats> getAllJobStats() {
        return new ConcurrentHashMap<>(jobStats);
    }

    /**
     * 清除执行统计
     */
    public void clearJobStats(String jobKey) {
        jobStats.remove(jobKey);
    }

    /**
     * 记录任务执行（不启用记录时使用）
     */
    private void recordJobExecution(JobDTO job, int status, String message) {
        if (Boolean.TRUE.equals(tsJobYmlProvider.ymlConfig().get("enableRecord"))) {
            TsJobRecordPO po = new TsJobRecordPO();
            po.setRecordStatus(status);
            po.setJobKey(job.getKey());
            tsJobRecordDAO.insert(po);
            log.info("[ts-job] Job [{}] skipped: {}", job.getKey(), message);
        }
    }

    /**
     * 执行任务，支持重试和超时
     */
    private ExecuteResult executeWithRetryAndTimeout(JobDTO job, TsJobRecordPO record) {
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

        String lastError = null;
        long startTime = System.currentTimeMillis();
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            if (attempt > 0) {
                // 重试前等待
                log.info("[ts-job] Job [{}] retry {}/{} after {}ms", 
                        job.getKey(), attempt, maxRetries, retryInterval);
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[ts-job] Job [{}] interrupted during retry, attempt {}", job.getKey(), attempt);
                    return ExecuteResult.failure("Interrupted during retry");
                }
            }

            // 记录每次尝试开始
            long attemptStartTime = System.currentTimeMillis();
            
            // 执行任务
            ExecuteResult result;
            try {
                if (timeoutSeconds > 0) {
                    result = executeWithTimeoutDetailed(job, timeoutSeconds);
                } else {
                    result = executeJobDetailed(job);
                }
            } catch (Exception e) {
                log.error("[ts-job] Job [{}] execution exception on attempt {}: {}", 
                        job.getKey(), attempt + 1, e.getMessage(), e);
                result = ExecuteResult.failure("Exception: " + e.getMessage());
            }
            
            long attemptDuration = System.currentTimeMillis() - attemptStartTime;
            
            if (result.isSuccess()) {
                long totalDuration = System.currentTimeMillis() - startTime;
                log.info("[ts-job] Job [{}] succeeded on attempt {}, total duration: {}ms", 
                        job.getKey(), attempt + 1, totalDuration);
                
                // 成功后检查是否触发慢任务告警
                checkSlowAlert(job, totalDuration);
                
                return ExecuteResult.success();
            }
            
            lastError = result.getErrorMessage();
            log.warn("[ts-job] Job [{}] failed on attempt {}: {}, duration: {}ms", 
                    job.getKey(), attempt + 1, lastError, attemptDuration);
        }
        
        long totalDuration = System.currentTimeMillis() - startTime;
        log.error("[ts-job] Job [{}] failed after {} attempts, total duration: {}ms, last error: {}", 
                job.getKey(), maxRetries + 1, totalDuration, lastError);
        
        // 发送失败告警
        if (jobAlertService != null) {
            try {
                jobAlertService.sendAlert(job, lastError, totalDuration);
            } catch (Exception e) {
                log.error("[ts-job] Failed to send alert for job [{}]: {}", job.getKey(), e.getMessage());
            }
        }
        
        return ExecuteResult.failure(lastError);
    }
    
    /**
     * 检查慢任务告警
     */
    private void checkSlowAlert(JobDTO job, long duration) {
        if (job.getSlowThreshold() > 0 && duration > job.getSlowThreshold() * 1000) {
            log.warn("[ts-job] Job [{}] slow execution: {}ms exceeds threshold {}s", 
                    job.getKey(), duration, job.getSlowThreshold());
            if (jobAlertService != null) {
                try {
                    jobAlertService.sendSlowAlert(job, duration);
                } catch (Exception e) {
                    log.error("[ts-job] Failed to send slow alert for job [{}]: {}", job.getKey(), e.getMessage());
                }
            }
        }
    }

    /**
     * 带超时执行任务（详细版）
     */
    private ExecuteResult executeWithTimeoutDetailed(JobDTO job, int timeoutSeconds) {
        Future<Boolean> future = taskExecutor.submit(() -> executeJob(job));
        try {
            Boolean result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            if (result) {
                return ExecuteResult.success();
            } else {
                return ExecuteResult.failure("Task returned false");
            }
        } catch (TimeoutException e) {
            log.error("[ts-job] Job [{}] timeout after {}s", job.getKey(), timeoutSeconds);
            future.cancel(true);
            
            // 发送超时告警
            if (jobAlertService != null) {
                try {
                    jobAlertService.sendTimeoutAlert(job, timeoutSeconds * 1000L);
                } catch (Exception ex) {
                    log.error("[ts-job] Failed to send timeout alert: {}", ex.getMessage());
                }
            }
            
            return ExecuteResult.failure("Timeout after " + timeoutSeconds + "s");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ts-job] Job [{}] interrupted", job.getKey());
            future.cancel(true);
            return ExecuteResult.failure("Interrupted");
        } catch (ExecutionException e) {
            log.error("[ts-job] Job [{}] execution exception: {}", job.getKey(), e.getMessage());
            return ExecuteResult.failure("Execution error: " + e.getMessage());
        }
    }

    /**
     * 执行单个任务（详细版）
     */
    private ExecuteResult executeJobDetailed(JobDTO job) {
        long startTime = System.currentTimeMillis();
        try {
            Object service = TsJobSpringUtils.getBean(job.getClassName());
            if (service == null) {
                log.error("[ts-job] Job [{}] bean not found: {}", job.getKey(), job.getClassName());
                return ExecuteResult.failure("Bean not found: " + job.getClassName());
            }
            
            Method method = ReflectionUtils.findMethod(service.getClass(), job.getaMethod().getName());
            if (method == null) {
                log.error("[ts-job] Job [{}] method not found: {}.{}", 
                        job.getKey(), job.getClassName(), job.getaMethod().getName());
                return ExecuteResult.failure("Method not found: " + job.getaMethod().getName());
            }
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, service);
            
            long duration = System.currentTimeMillis() - startTime;
            return ExecuteResult.success();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[ts-job] Job [{}] execution failed after {}ms: {}", 
                    job.getKey(), duration, e.getMessage(), e);
            return ExecuteResult.failure(e.getMessage());
        }
    }
    
    /**
     * 保留兼容性的旧方法
     * @deprecated 请使用 {@link #executeWithTimeoutDetailed(JobDTO, int)}
     */
    @Deprecated
    private boolean executeWithTimeout(JobDTO job, int timeoutSeconds) {
        Future<?> future = taskExecutor.submit(() -> executeJob(job));
        try {
            return (boolean) future.get(timeoutSeconds, TimeUnit.SECONDS);
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
     * 获取正在运行的任务数量
     */
    public int getRunningJobCount(String jobKey) {
        return (int) runningJobs.entrySet().stream()
                .filter(e -> e.getKey().equals(jobKey))
                .count();
    }

    /**
     * 获取所有正在运行的任务
     */
    public List<RunningJob> getRunningJobs() {
        return new ArrayList<>(runningJobs.values());
    }

    /**
     * 正在运行的任务信息
     */
    @lombok.Data
    public static class RunningJob {
        private String recordId;
        private long startTime;
    }

    /**
     * 执行结果
     */
    @lombok.Data
    public static class ExecuteResult {
        private boolean success;
        private String errorMessage;

        public static ExecuteResult success() {
            ExecuteResult result = new ExecuteResult();
            result.setSuccess(true);
            return result;
        }

        public static ExecuteResult failure(String message) {
            ExecuteResult result = new ExecuteResult();
            result.setSuccess(false);
            result.setErrorMessage(message);
            return result;
        }
    }

    /**
     * 任务执行统计
     */
    @lombok.Data
    public static class JobExecutionStats {
        private long totalExecutions = 0;
        private long successCount = 0;
        private long failureCount = 0;
        private long totalDuration = 0;
        private long maxDuration = 0;
        private long minDuration = Long.MAX_VALUE;
        private double avgDuration = 0;
        private long lastExecutionTime = 0;

        public void recordExecution(boolean success, long duration) {
            totalExecutions++;
            if (success) {
                successCount++;
            } else {
                failureCount++;
            }
            totalDuration += duration;
            maxDuration = Math.max(maxDuration, duration);
            minDuration = Math.min(minDuration, duration);
            avgDuration = (double) totalDuration / totalExecutions;
            lastExecutionTime = System.currentTimeMillis();
        }

        public double getSuccessRate() {
            return totalExecutions > 0 ? (double) successCount / totalExecutions * 100 : 0;
        }
    }
}