package com.ts.config;

import com.ts.service.TaskService;
import com.ts.service.TaskSuspendService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

/**
 * 调度任务执行器
 * 支持暂停检查、失败重试、超时控制等
 * 
 * @author yue.wu
 * @date 2022/5/5 9:48
 */
@Slf4j
@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@AllArgsConstructor
public class TsJobTaskJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().getName();
        
        // 检查任务是否被暂停
        TaskSuspendService suspendService = null;
        try {
            suspendService = TsJobApplicationContextRegister.getApplicationContext().getBean(TaskSuspendService.class);
        } catch (Exception e) {
            // 服务可能未初始化，忽略
        }
        
        if (suspendService != null && suspendService.isSuspended(jobKey)) {
            log.info("[ts-job-TaskJob] job [{}] is suspended, skip execution", jobKey);
            return;
        }

        TaskService taskService =
                TsJobApplicationContextRegister.getApplicationContext().getBean(TaskService.class);

        try {
            log.debug("[ts-job-TaskJob] start executing job: {}", jobKey);
            taskService.runTaskByQuartzKey(context.getJobDetail().getKey());
        } catch (Exception e) {
            log.error("[ts-job-TaskJob] job execute error for [{}]: {}", jobKey, e.getMessage(), e);
        }
    }
}
