package com.ts.config;

import com.ts.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

/**
 * @author yue.wu
 * @description TODO
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
        TaskService taskService =
                TsJobApplicationContextRegister.getApplicationContext().getBean(TaskService.class);
//        log.info("[ts-job-TaskJob] start task job key {}", context.getJobDetail().getKey().toString());

        try {
            taskService.runTaskByQuartzKey(context.getJobDetail().getKey());
        } catch (Exception e) {
            log.error("[ts-job-TaskJob] job execute error because {}", e.getMessage());
        }
    }
}
