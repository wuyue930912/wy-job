package com.ts.api;

import cn.hutool.core.bean.BeanUtil;
import com.ts.config.TsJobConfig;
import com.ts.config.TsJobTaskJob;
import com.ts.dto.JobDTO;
import com.ts.dto.JobRecordBI;
import com.ts.mapper.TsJobDAO;
import com.ts.mapper.TsJobRecordDAO;
import com.ts.po.TsJobPO;
import com.ts.util.TsJobSpringUtils;
import com.ts.vo.TsJobPageParamVO;
import com.ts.vo.TsJobPageResultVO;
import com.ts.vo.TsJobResponseVO;
import com.ts.vo.TsJobVO;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.ts.constant.JobConstant.SCHEDULER_FACTORY;
import static com.ts.constant.JobConstant.TRIGGER_GROUP_NAME;

/**
 * @author yue.wu
 * @description api接口
 * @date 2023/5/4 16:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ts-job")
public class TsJobApi {
    private final Logger log = LoggerFactory.getLogger(TsJobApi.class);

    private final TsJobDAO tsJobDAO;

    private final TsJobRecordDAO tsJobRecordDAO;

    /**
     * 根据KEY执行JOB对应的方法
     *
     * @author yue.wu
     * @date 2023/5/4 16:31
     */
    @GetMapping("/run-job")
    public TsJobResponseVO<String> runJob(String jobKey) {
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (Objects.nonNull(job)) {
            try {
                Object service = TsJobSpringUtils.getBean(job.getClassName());
                // 传递需要执行的方法
                Method method = ReflectionUtils.findMethod(service.getClass(), job.getaMethod().getName());
                log.info("[ts-job-TsJobApi] start run job {}, class {}, real method {} ", jobKey, service.getClass().getName(), method.getName());
                ReflectionUtils.invokeMethod(method, service);
                return new TsJobResponseVO<>(200L, "执行成功");
            } catch (Exception e) {
                return new TsJobResponseVO<>(500L, "执行失败" + e.getMessage());
            }
        }
        return new TsJobResponseVO<>(500L, "调度未注册");
    }

    /**
     * 根据KEY开启对应的调度
     *
     * @author yue.wu
     * @date 2023/5/5 15:22
     */
    @GetMapping("/start-scheduler")
    public TsJobResponseVO<String> startJob(String jobKey) {
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (Objects.nonNull(job)) {
            try {
                TsJobPO po = tsJobDAO.selectByKey(job.getKey());
                if (Objects.isNull(po)) {
                    return new TsJobResponseVO<>(400L, "JOB不存在");
                }
                po.setVersion(0);
                tsJobDAO.updateById(po);

                Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();
                JobDetail jobDetail = JobBuilder.newJob(TsJobTaskJob.class)
                        .withIdentity(job.getKey(), TRIGGER_GROUP_NAME)
                        .build();
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(job.getKey(), TRIGGER_GROUP_NAME);
                triggerBuilder.startNow();
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(po.getCron()));
                CronTrigger trigger = (CronTrigger) triggerBuilder.build();
                scheduler.scheduleJob(jobDetail, trigger);
                if (!scheduler.isShutdown()) {
                    scheduler.start();
                }
                return new TsJobResponseVO<>(200L, "处理成功");
            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobConfig] Quartz = error : {}", e.getMessage());
                return new TsJobResponseVO<>(500L, "处理失败" + e.getMessage());

            }
        }
        return new TsJobResponseVO<>(500L, "调度未注册");
    }

    /**
     * 根据KEY停止对应的调度
     *
     * @author yue.wu
     * @date 2023/5/5 15:22
     */
    @GetMapping("/stop-scheduler")
    public TsJobResponseVO<String> stopJob(String jobKey) {
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (Objects.nonNull(job)) {
            try {
                TsJobPO po = tsJobDAO.selectByKey(job.getKey());
                if (Objects.isNull(po)) {
                    return new TsJobResponseVO<>(400L, "JOB不存在");
                }
                po.setVersion(1);
                tsJobDAO.updateById(po);

                Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();
                TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, TRIGGER_GROUP_NAME);
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(JobKey.jobKey(jobKey, TRIGGER_GROUP_NAME));
                return new TsJobResponseVO<>(200L, "处理成功");

            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobApi] stop job fail because {}", e.getMessage());
                return new TsJobResponseVO<>(500L, "处理失败" + e.getMessage());
            }
        }
        return new TsJobResponseVO<>(500L, "调度任务未注册");
    }

    /**
     * 创建调度任务
     *
     * @author yue.wu
     * @date 2023/5/5 15:29
     */
    @PostMapping("/add-job")
    public TsJobResponseVO<String> addJob(@RequestBody TsJobPO po) {
        log.info("[ts-job-TsJobApi] start add job {}, key {}, time {} ", po.getJobName(), po.getJobKey(), new Date());
        po.setVersion(1);
        try {
            tsJobDAO.insert(po);
        } catch (Exception e) {
            return new TsJobResponseVO<>(500L, "新增失败" + e.getMessage());
        }
        return new TsJobResponseVO<>(200L, "处理成功");
    }

    @PostMapping("/edit-job")
    public TsJobResponseVO<String> editJob(@RequestBody TsJobPO po) throws InterruptedException {
        log.info("[ts-job-TsJobApi] start edit job {}, key {}, time {} ", po.getJobName(), po.getJobKey(), new Date());
        try {
            tsJobDAO.updateById(po);
        } catch (Exception e) {
            return new TsJobResponseVO<>(500L, "修改失败" + e.getMessage());
        }

        stopJob(po.getJobKey());
        Thread.sleep(1000);
        startJob(po.getJobKey());

        return new TsJobResponseVO<>(200L, "处理成功");
    }

    @GetMapping("/del-job")
    public TsJobResponseVO<String> delJob(String id) {
        log.info("[ts-job-TsJobApi] start del job id {}, time {} ", id, new Date());
        TsJobPO po = tsJobDAO.selectById(id);
        if (Objects.isNull(po)) {
            return new TsJobResponseVO<>(400L, "JOB不存在");
        }
        String jobKey = po.getJobKey();
        JobDTO job = TsJobConfig.jobs.get(jobKey);
        if (Objects.nonNull(job)) {
            try {
                Scheduler scheduler = SCHEDULER_FACTORY.getScheduler();
                TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, TRIGGER_GROUP_NAME);
                scheduler.pauseTrigger(triggerKey);
                scheduler.unscheduleJob(triggerKey);
                scheduler.deleteJob(JobKey.jobKey(jobKey, TRIGGER_GROUP_NAME));
                return new TsJobResponseVO<>(200L, "处理成功");
            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobApi] stop job fail because {}", e.getMessage());
                return new TsJobResponseVO<>(500L, "处理失败" + e.getMessage());
            }
        }
        tsJobDAO.deleteById(id);

        return new TsJobResponseVO<>(200L, "处理成功");
    }

    @PostMapping("/search-job")
    public TsJobResponseVO<TsJobPageResultVO<List<TsJobVO>>> searchJob(@RequestBody TsJobPageParamVO<String> vo) {
        log.info("[ts-job-TsJobApi] start search job time {} ", new Date());
        List<TsJobPO> list = tsJobDAO.selectByPage((vo.getPageIndex() - 1) * vo.getPageSize(),
                vo.getPageSize());
        Long count = tsJobDAO.selectCountByPage();
        Long totalPage = (count + vo.getPageSize() - 1) / vo.getPageSize();

        List<TsJobVO> result = new ArrayList<>();
        list.forEach(po -> {
            TsJobVO tsJobVO = new TsJobVO();
            BeanUtil.copyProperties(po, tsJobVO);
            if (po.getVersion().equals(1)) {
                // stop
                tsJobVO.setVersion("关闭");
            } else if (po.getVersion().equals(0)) {
                // start
                tsJobVO.setVersion("开启");
            } else if (po.getVersion().equals(3)) {
                // not start
                tsJobVO.setVersion("未开始");
            }
            result.add(tsJobVO);
        });

        TsJobPageResultVO<List<TsJobVO>> data = TsJobPageResultVO.<List<TsJobVO>>builder()
                .result(result)
                .total(totalPage)
                .build();
        return new TsJobResponseVO<>(200L, "处理成功", data);
    }

    @GetMapping("/get-mem")
    public TsJobResponseVO<Integer> getMem() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        GlobalMemory memory = hal.getMemory();
        long used = memory.getTotal() - memory.getAvailable();
        int result = (int) (((double) used / (double) memory.getTotal()) * 100);
        return new TsJobResponseVO<>(200L, "处理成功", result);
    }

    @GetMapping("/get-cpu")
    public TsJobResponseVO<Integer> getCpu() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(500);

        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softIrq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long ioWait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = Math.max(user + nice + cSys + idle + ioWait + irq + softIrq + steal, 0);
        int result = Math.max((int) (100d * user / (double) totalCpu) + (int) (100d * cSys / (double) totalCpu), 0);
        return new TsJobResponseVO<>(200L, "处理成功", result);
    }

    @GetMapping("/get-record-bi")
    public TsJobResponseVO<List<JobRecordBI>> getRecordBI() {
        List<JobRecordBI> result = new ArrayList<>();
        tsJobRecordDAO.selectRecordBI().forEach(po -> {
            JobRecordBI dto = new JobRecordBI();
            dto.setValue(po.getValue());
            dto.setName(po.getName().equals("1") ? "成功" : (po.getName().equals("2") ? "失败" : "未执行"));
            result.add(dto);
        });

        return new TsJobResponseVO<>(200L, "处理成功", result);
    }
}