package com.ts.service;

import com.ts.dto.JobDTO;
import com.ts.po.TsJobPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import static com.ts.constant.JobConstant.SCHEDULER_FACTORY;
import static com.ts.constant.JobConstant.TRIGGER_GROUP_NAME;

/**
 * 任务失败策略服务
 * 根据配置的失败策略执行不同操作
 * 
 * @author wy-job
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FailStrategyService {

    private final TaskSuspendService taskSuspendService;
    
    /**
     * 失败策略常量
     */
    public static final int STRATEGY_STOP = 1;      // 停止调度
    public static final int STRATEGY_RETRY = 2;     // 继续重试（默认）
    public static final int STRATEGY_IGNORE = 3;    // 忽略继续
    
    /**
     * 处理任务失败
     * 根据失败策略执行相应操作
     * 
     * @param job 任务DTO
     * @param errorMessage 错误信息
     */
    public void handleJobFailure(JobDTO job, String errorMessage) {
        if (job == null) {
            return;
        }
        
        int strategy = job.getFailStrategy();
        String jobKey = job.getKey();
        
        switch (strategy) {
            case STRATEGY_STOP:
                // 停止调度 - 暂停任务
                log.warn("[ts-job] Job [{}] failed with strategy STOP, suspending job", jobKey);
                try {
                    taskSuspendService.suspendJob(jobKey);
                } catch (Exception e) {
                    log.error("[ts-job] Failed to suspend job [{}]: {}", jobKey, e.getMessage());
                }
                break;
                
            case STRATEGY_IGNORE:
                // 忽略继续 - 记录日志但不做任何操作
                log.warn("[ts-job] Job [{}] failed with strategy IGNORE: {}", jobKey, errorMessage);
                break;
                
            case STRATEGY_RETRY:
            default:
                // 继续重试 - 默认行为，不做任何特殊处理
                log.debug("[ts-job] Job [{}] failed, will retry automatically", jobKey);
                break;
        }
    }
    
    /**
     * 根据数据库PO处理任务失败
     * 
     * @param po 任务PO
     * @param errorMessage 错误信息
     */
    public void handleJobFailure(TsJobPO po, String errorMessage) {
        if (po == null) {
            return;
        }
        
        int strategy = po.getFailStrategy() != null ? po.getFailStrategy() : STRATEGY_RETRY;
        String jobKey = po.getJobKey();
        
        switch (strategy) {
            case STRATEGY_STOP:
                log.warn("[ts-job] Job [{}] failed with strategy STOP, suspending job", jobKey);
                try {
                    taskSuspendService.suspendJob(jobKey);
                } catch (Exception e) {
                    log.error("[ts-job] Failed to suspend job [{}]: {}", jobKey, e.getMessage());
                }
                break;
                
            case STRATEGY_IGNORE:
                log.warn("[ts-job] Job [{}] failed with strategy IGNORE: {}", jobKey, errorMessage);
                break;
                
            case STRATEGY_RETRY:
            default:
                log.debug("[ts-job] Job [{}] failed, will retry automatically", jobKey);
                break;
        }
    }
    
    /**
     * 获取失败策略描述
     * 
     * @param strategy 策略值
     * @return 策略描述
     */
    public String getStrategyDesc(int strategy) {
        switch (strategy) {
            case STRATEGY_STOP:
                return "停止调度";
            case STRATEGY_IGNORE:
                return "忽略继续";
            case STRATEGY_RETRY:
            default:
                return "继续重试";
        }
    }
}