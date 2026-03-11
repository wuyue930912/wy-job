package com.ts.service;

import com.ts.config.TsJobConfig;
import com.ts.dto.JobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务暂停/恢复服务
 * 支持暂停指定任务，暂停后任务不会被调度执行
 * 
 * @author wy-job
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSuspendService {

    /**
     * 暂停的任务集合（key: jobKey, value: 是否暂停）
     */
    private final ConcurrentHashMap<String, Boolean> suspendedJobs = new ConcurrentHashMap<>();

    /**
     * 暂停任务
     * @param jobKey 任务key
     * @return 是否成功
     */
    public boolean suspendJob(String jobKey) {
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (job == null) {
            log.warn("[ts-job] Suspend failed: job [{}] not found", jobKey);
            return false;
        }
        
        suspendedJobs.put(jobKey, true);
        log.info("[ts-job] Job [{}] has been suspended", jobKey);
        return true;
    }

    /**
     * 恢复任务
     * @param jobKey 任务key
     * @return 是否成功
     */
    public boolean resumeJob(String jobKey) {
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (job == null) {
            log.warn("[ts-job] Resume failed: job [{}] not found", jobKey);
            return false;
        }
        
        suspendedJobs.remove(jobKey);
        log.info("[ts-job] Job [{}] has been resumed", jobKey);
        return true;
    }

    /**
     * 检查任务是否被暂停
     * @param jobKey 任务key
     * @return 是否暂停
     */
    public boolean isSuspended(String jobKey) {
        return suspendedJobs.getOrDefault(jobKey, false);
    }

    /**
     * 获取所有暂停的任务
     * @return 暂停的任务key列表
     */
    public java.util.List<String> getSuspendedJobs() {
        return new java.util.ArrayList<>(suspendedJobs.keySet());
    }

    /**
     * 批量暂停任务
     * @param jobKeys 任务key列表
     * @return 成功数量
     */
    public int suspendJobs(java.util.List<String> jobKeys) {
        int count = 0;
        for (String jobKey : jobKeys) {
            if (suspendJob(jobKey)) {
                count++;
            }
        }
        log.info("[ts-job] Batch suspend: {} jobs suspended out of {}", count, jobKeys.size());
        return count;
    }

    /**
     * 批量恢复任务
     * @param jobKeys 任务key列表
     * @return 成功数量
     */
    public int resumeJobs(java.util.List<String> jobKeys) {
        int count = 0;
        for (String jobKey : jobKeys) {
            if (resumeJob(jobKey)) {
                count++;
            }
        }
        log.info("[ts-job] Batch resume: {} jobs resumed out of {}", count, jobKeys.size());
        return count;
    }
}