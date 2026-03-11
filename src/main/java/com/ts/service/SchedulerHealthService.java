package com.ts.service;

import com.ts.config.TsJobConfig;
import com.ts.dto.JobDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调度健康检查服务
 * 提供调度器的健康状态、任务注册情况等信息
 * 
 * @author wy-job
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerHealthService {

    @Autowired(required = false)
    private TaskSuspendService taskSuspendService;

    /**
     * 获取调度器健康状态
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        // 调度器状态
        health.put("schedulerStatus", "running");
        
        // 已注册任务数
        health.put("registeredJobCount", TsJobConfig.jobs.size());
        
        // 暂停任务数
        int suspendedCount = 0;
        if (taskSuspendService != null) {
            suspendedCount = taskSuspendService.getSuspendedJobs().size();
        }
        health.put("suspendedJobCount", suspendedCount);
        
        // 运行中任务数
        // 通过统计API可以获取
        
        // 任务健康状态列表
        List<Map<String, Object>> jobHealthList = new ArrayList<>();
        for (Map.Entry<String, JobDTO> entry : TsJobConfig.jobs.entrySet()) {
            Map<String, Object> jobHealth = new HashMap<>();
            jobHealth.put("jobKey", entry.getKey());
            jobHealth.put("jobName", entry.getValue().getDescription());
            jobHealth.put("isSuspended", taskSuspendService != null && taskSuspendService.isSuspended(entry.getKey()));
            jobHealthList.add(jobHealth);
        }
        health.put("jobHealthList", jobHealthList);
        
        return health;
    }

    /**
     * 获取调度器摘要信息
     */
    public Map<String, Object> getSchedulerSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalJobs", TsJobConfig.jobs.size());
        
        if (taskSuspendService != null) {
            List<String> suspended = taskSuspendService.getSuspendedJobs();
            summary.put("activeJobs", TsJobConfig.jobs.size() - suspended.size());
            summary.put("suspendedJobs", suspended.size());
        } else {
            summary.put("activeJobs", TsJobConfig.jobs.size());
            summary.put("suspendedJobs", 0);
        }
        
        return summary;
    }
}