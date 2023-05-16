package com.ts.service;

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

/**
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

    public void runTaskByQuartzKey(JobKey key) {
        String jobKey = key.toString().substring(key.toString().indexOf(".") + 1);
//        log.info("[ts-job-TaskService] start run job [{}] at [{}]", jobKey, new Date());
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (tsJobYmlProvider.ymlConfig().get("enableRecord").equals(true)) {
            if (Objects.nonNull(job)) {
                // 记录执行记录，状态执行中
                TsJobRecordPO po = new TsJobRecordPO();
                // 执行中
                po.setRecordStatus(3);
                po.setJobKey(job.getKey());
                tsJobRecordDAO.insert(po);

                try {
                    Object service = TsJobSpringUtils.getBean(job.getClassName());
                    // 传递需要执行的方法
                    Method method = ReflectionUtils.findMethod(service.getClass(), job.getaMethod().getName());
//                log.info("[ts-job-TaskService] start run job {}, class {}, real method {} ", jobKey, service.getClass().getName(), method.getName());
                    ReflectionUtils.invokeMethod(method, service);
                    po.setEndTime(new Timestamp(System.currentTimeMillis()));
                    // 执行成功
                    po.setRecordStatus(1);
                    tsJobRecordDAO.updateById(po);
                } catch (Exception e) {
                    log.warn("[ts-job-TaskService] job run fail");
                    // 执行失败
                    po.setEndTime(new Timestamp(System.currentTimeMillis()));
                    po.setRecordStatus(2);
                    tsJobRecordDAO.updateById(po);
                }
            }
        }
    }
}
