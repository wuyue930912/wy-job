package com.ts.config;

import com.ts.annotation.TsJOB;
import com.ts.dto.JobDTO;
import com.ts.mapper.TsJobDAO;
import com.ts.po.TsJobPO;
import com.ts.provider.TsJobYmlProvider;
import com.ts.util.TsJobSpringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.quartz.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.ts.constant.JobConstant.SCHEDULER_FACTORY;
import static com.ts.constant.JobConstant.TRIGGER_GROUP_NAME;

/**
 * @author yue.wu
 * @description 加载全部JOB
 * @date 2023/5/4 16:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
@MapperScan("com.ts.mapper")
public class TsJobConfig {

    private final TsJobDAO tsJobDAO;

    // 所有使用了TsJob注解的方法
    public static Map<String, JobDTO> jobs = new ConcurrentHashMap<>();

    @Resource
    private final TsJobSpringUtils springUtils;

    @Resource
    private final TsJobYmlProvider tsJobYmlProvider;

    @PostConstruct
    public void init() throws ClassNotFoundException {
        if (Boolean.TRUE.equals(tsJobYmlProvider.ymlConfig().get("enableBanner"))) {
            log.info("\n" +
                    "TTTTTTTTTTTTTTTTTTTTTTT  SSSSSSSSSSSSSSS                            JJJJJJJJJJJ    OOOOOOOOO    BBBBBBBBBBBBBBBBB   \n" +
                    "T:::::::::::::::::::::TSS:::::::::::::::S                           J:::::::::J  OO:::::::::OO  B::::::::::::::::B  \n" +
                    "T:::::::::::::::::::::S:::::SSSSSS::::::S                           J:::::::::JOO:::::::::::::OOB::::::BBBBBB:::::B \n" +
                    "T:::::TT:::::::TT:::::S:::::S     SSSSSSS                           JJ:::::::JO:::::::OOO:::::::BB:::::B     B:::::B\n" +
                    "TTTTTT  T:::::T  TTTTTS:::::S                                         J:::::J O::::::O   O::::::O B::::B     B:::::B\n" +
                    "        T:::::T       S:::::S                                         J:::::J O:::::O     O:::::O B::::B     B:::::B\n" +
                    "        T:::::T        S::::SSSS                                      J:::::J O:::::O     O:::::O B::::BBBBBB:::::B \n" +
                    "        T:::::T         SS::::::SSSSS     ---------------             J:::::j O:::::O     O:::::O B:::::::::::::BB  \n" +
                    "        T:::::T           SSS::::::::SS   -:::::::::::::-             J:::::J O:::::O     O:::::O B::::BBBBBB:::::B \n" +
                    "        T:::::T              SSSSSS::::S  --------------- JJJJJJJ     J:::::J O:::::O     O:::::O B::::B     B:::::B\n" +
                    "        T:::::T                   S:::::S                 J:::::J     J:::::J O:::::O     O:::::O B::::B     B:::::B\n" +
                    "        T:::::T                   S:::::S                 J::::::J   J::::::J O::::::O   O::::::O B::::B     B:::::B\n" +
                    "      TT:::::::TT     SSSSSSS     S:::::S                 J:::::::JJJ:::::::J O:::::::OOO:::::::BB:::::BBBBBB::::::B\n" +
                    "      T:::::::::T     S::::::SSSSSS:::::S                  JJ:::::::::::::JJ   OO:::::::::::::OOB:::::::::::::::::B \n" +
                    "      T:::::::::T     S:::::::::::::::SS                     JJ:::::::::JJ       OO:::::::::OO  B::::::::::::::::B  \n" +
                    "      TTTTTTTTTTT      SSSSSSSSSSSSSSS                         JJJJJJJJJ           OOOOOOOOO    BBBBBBBBBBBBBBBBB   \n" +
                    "                                                                                                         version 1.1.12");
        }

        // 获取所有使用TsJob注解的方法
        Map<String, Object> map = springUtils.getApplicationContext();
        map.keySet().forEach(key -> {
            Class aClass = map.get(key).getClass();
            List<Method> methodList = Arrays.asList(aClass.getMethods());
            methodList.forEach(aMethod -> {
                if (aMethod.getAnnotation(TsJOB.class) != null) {
                    TsJOB annotation = aMethod.getAnnotation(TsJOB.class);
                    JobDTO dto = new JobDTO();
                    dto.setaClass(aClass);
                    String[] classNameSplit = aClass.getName().split("\\.");
                    String className = classNameSplit[classNameSplit.length - 1];
                    dto.setClassName(lowerFirst(className));
                    dto.setaMethod(aMethod);
                    dto.setKey(annotation.key());
                    dto.setDescription(annotation.description());
                    dto.setRetryCount(annotation.retryCount());
                    dto.setTimeout(annotation.timeout());
                    dto.setRetryInterval(annotation.retryInterval());
                    
                    // 新增：并发控制
                    dto.setMaxConcurrent(annotation.maxConcurrent());
                    
                    // 新增：任务依赖
                    if (annotation.dependencies() != null && annotation.dependencies().length > 0) {
                        dto.setDependencies(Arrays.asList(annotation.dependencies()));
                    }
                    dto.setDependencyCheckSuccess(annotation.dependencyCheckSuccess());
                    
                    // 新增：慢查询阈值
                    dto.setSlowThreshold(annotation.slowThreshold());
                    
                    log.info("[ts-job-TsJobConfig] class [{}] method [{}] key [{}], retryCount [{}], timeout [{}]s, maxConcurrent [{}], dependencies [{}], slowThreshold [{}]s", 
                            className, aMethod, annotation.key(), annotation.retryCount(), 
                            annotation.timeout(), annotation.maxConcurrent(),
                            Arrays.toString(annotation.dependencies()),
                            annotation.slowThreshold());

                    jobs.put(annotation.key(), dto);
                }
            });
        });

        List<TsJobPO> jobPOS = tsJobDAO.selectAll();
        jobPOS.forEach(job -> {
            try {
                log.info("[ts-job-TsJobConfig] start init quartz job [{}] TRIGGER_GROUP_NAME [{}]", job.getJobKey(), TRIGGER_GROUP_NAME);
                if (Objects.isNull(jobs.get(job.getJobKey()))) {
                    log.warn("[ts-job-TsJobConfig] job [{}] not registered, skipped", job.getJobKey());
                    return;
                }
                if (job.getVersion().equals(1)) {
                    log.info("[ts-job-TsJobConfig] job [{}] is disabled", job.getJobKey());
                    return;
                }
                Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();
                JobDetail jobDetail = JobBuilder.newJob(TsJobTaskJob.class)
                        .withIdentity(job.getJobKey(), TRIGGER_GROUP_NAME)
                        .build();
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(job.getJobKey(), TRIGGER_GROUP_NAME);
                triggerBuilder.startNow();
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()));
                CronTrigger trigger = (CronTrigger) triggerBuilder.build();
                scheduler.scheduleJob(jobDetail, trigger);
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobConfig] Quartz error : {}", e.getMessage(), e);
            }
        });

    }

    private static String lowerFirst(String str) {
        char[] cs = str.toCharArray();
        cs[0] += 32;
        return String.valueOf(cs);
    }

}