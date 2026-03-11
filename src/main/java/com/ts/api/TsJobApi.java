package com.ts.api;

import cn.hutool.core.bean.BeanUtil;
import com.ts.config.TsJobConfig;
import com.ts.config.TsJobTaskJob;
import com.ts.dto.JobDTO;
import com.ts.dto.JobRecordBI;
import com.ts.mapper.TsJobDAO;
import com.ts.mapper.TsJobRecordDAO;
import com.ts.po.TsJobPO;
import com.ts.po.TsJobRecordPO;
import com.ts.service.SchedulerHealthService;
import com.ts.service.TaskGroupService;
import com.ts.service.TaskService;
import com.ts.service.TaskSuspendService;
import com.ts.service.FailStrategyService;
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
import java.util.*;
import java.util.stream.Collectors;

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

    private final TaskService taskService;

    private final TaskSuspendService taskSuspendService;

    private final SchedulerHealthService schedulerHealthService;
    
    private final TaskGroupService taskGroupService;
    
    private final FailStrategyService failStrategyService;

    /**
     * 根据KEY执行JOB对应的方法
     *
     * @author yue.wu
     * @date 2023/5/4 16:31
     */
    @GetMapping("/run-job")
    public TsJobResponseVO<String> runJob(String jobKey) {
        // 检查任务是否被暂停
        if (taskSuspendService.isSuspended(jobKey)) {
            log.warn("[ts-job] Job [{}] is suspended, cannot run", jobKey);
            return new TsJobResponseVO<>(400L, "任务已暂停，无法执行");
        }
        
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
                log.error("[ts-job-TsJobApi] run job error: {}", e.getMessage(), e);
                return new TsJobResponseVO<>(500L, "执行失败: " + e.getMessage());
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
                log.info("[ts-job-TsJobApi] started scheduler for job: {}", jobKey);
                return new TsJobResponseVO<>(200L, "处理成功");
            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobApi] start scheduler error: {}", e.getMessage(), e);
                return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());

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
                log.info("[ts-job-TsJobApi] stopped scheduler for job: {}", jobKey);
                return new TsJobResponseVO<>(200L, "处理成功");

            } catch (SchedulerException e) {
                log.error("[ts-job-TsJobApi] stop job fail because {}", e.getMessage(), e);
                return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
            }
        }
        return new TsJobResponseVO<>(500L, "调度任务未注册");
    }

    /**
     * 暂停任务（运行时暂停，不删除调度）
     */
    @GetMapping("/suspend-job")
    public TsJobResponseVO<String> suspendJob(String jobKey) {
        try {
            boolean result = taskSuspendService.suspendJob(jobKey);
            if (result) {
                return new TsJobResponseVO<>(200L, "任务已暂停");
            } else {
                return new TsJobResponseVO<>(400L, "任务不存在");
            }
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] suspend job error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "暂停失败: " + e.getMessage());
        }
    }

    /**
     * 恢复任务
     */
    @GetMapping("/resume-job")
    public TsJobResponseVO<String> resumeJob(String jobKey) {
        try {
            boolean result = taskSuspendService.resumeJob(jobKey);
            if (result) {
                return new TsJobResponseVO<>(200L, "任务已恢复");
            } else {
                return new TsJobResponseVO<>(400L, "任务不存在");
            }
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] resume job error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "恢复失败: " + e.getMessage());
        }
    }

    /**
     * 获取暂停的任务列表
     */
    @GetMapping("/get-suspended-jobs")
    public TsJobResponseVO<List<String>> getSuspendedJobs() {
        try {
            List<String> suspendedJobs = taskSuspendService.getSuspendedJobs();
            return new TsJobResponseVO<>(200L, "处理成功", suspendedJobs);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get suspended jobs error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
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
            return new TsJobResponseVO<>(200L, "处理成功");
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] add job error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "新增失败: " + e.getMessage());
        }
    }

    @PostMapping("/edit-job")
    public TsJobResponseVO<String> editJob(@RequestBody TsJobPO po) throws InterruptedException {
        log.info("[ts-job-TsJobApi] start edit job {}, key {}, time {} ", po.getJobName(), po.getJobKey(), new Date());
        try {
            tsJobDAO.updateById(po);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] edit job error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "修改失败: " + e.getMessage());
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
        
        // 清除暂停状态
        taskSuspendService.resumeJob(jobKey);
        
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
                log.error("[ts-job-TsJobApi] stop job fail because {}", e.getMessage(), e);
                return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
            }
        }
        tsJobDAO.deleteById(id);

        return new TsJobResponseVO<>(200L, "处理成功");
    }

    @PostMapping("/search-job")
    public TsJobResponseVO<TsJobPageResultVO<List<TsJobVO>>> searchJob(@RequestBody TsJobPageParamVO<String> vo) {
        log.info("[ts-job-TsJobApi] start search job time {} ", new Date());
        try {
            List<TsJobPO> list = tsJobDAO.selectByPage((vo.getPageIndex() - 1) * vo.getPageSize(),
                    vo.getPageSize());
            Long count = tsJobDAO.selectCountByPage();
            Long totalPage = (count + vo.getPageSize() - 1) / vo.getPageSize();

            List<TsJobVO> result = new ArrayList<>();
            for (TsJobPO po : list) {
                TsJobVO tsJobVO = new TsJobVO();
                BeanUtil.copyProperties(po, tsJobVO);
                if (po.getVersion().equals(1)) {
                    tsJobVO.setVersion("关闭");
                } else if (po.getVersion().equals(0)) {
                    tsJobVO.setVersion("开启");
                } else if (po.getVersion().equals(3)) {
                    tsJobVO.setVersion("未开始");
                }
                // 添加任务运行统计
                TaskService.JobExecutionStats stats = taskService.getJobStats(po.getJobKey());
                if (stats != null) {
                    tsJobVO.setTotalCount(stats.getTotalExecutions());
                    tsJobVO.setSuccessRate(stats.getSuccessRate());
                }
                // 添加暂停状态
                tsJobVO.setSuspended(taskSuspendService.isSuspended(po.getJobKey()));
                result.add(tsJobVO);
            }

            TsJobPageResultVO<List<TsJobVO>> data = TsJobPageResultVO.<List<TsJobVO>>builder()
                    .result(result)
                    .total(totalPage)
                    .build();
            return new TsJobResponseVO<>(200L, "处理成功", data);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] search job error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统内存使用率
     */
    @GetMapping("/get-mem")
    public TsJobResponseVO<Integer> getMem() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            GlobalMemory memory = hal.getMemory();
            long used = memory.getTotal() - memory.getAvailable();
            int result = (int) (((double) used / (double) memory.getTotal()) * 100);
            return new TsJobResponseVO<>(200L, "处理成功", result);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get mem error: {}", e.getMessage());
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统CPU使用率
     */
    @GetMapping("/get-cpu")
    public TsJobResponseVO<Integer> getCpu() {
        try {
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
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get cpu error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行统计BI数据
     */
    @GetMapping("/get-record-bi")
    public TsJobResponseVO<List<JobRecordBI>> getRecordBI() {
        try {
            List<JobRecordBI> result = new ArrayList<>();
            tsJobRecordDAO.selectRecordBI().forEach(po -> {
                JobRecordBI dto = new JobRecordBI();
                dto.setValue(po.getValue());
                dto.setName(po.getName().equals("1") ? "成功" : (po.getName().equals("2") ? "失败" : "未执行"));
                result.add(dto);
            });
            return new TsJobResponseVO<>(200L, "处理成功", result);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get record bi error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务的执行统计
     */
    @GetMapping("/get-all-stats")
    public TsJobResponseVO<Map<String, TaskService.JobExecutionStats>> getAllStats() {
        try {
            Map<String, TaskService.JobExecutionStats> stats = taskService.getAllJobStats();
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get all stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定任务的执行统计
     */
    @GetMapping("/get-job-stats")
    public TsJobResponseVO<TaskService.JobExecutionStats> getJobStats(String jobKey) {
        try {
            TaskService.JobExecutionStats stats = taskService.getJobStats(jobKey);
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get job stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取正在运行的任务列表
     */
    @GetMapping("/get-running-jobs")
    public TsJobResponseVO<List<TaskService.RunningJob>> getRunningJobs() {
        try {
            List<TaskService.RunningJob> runningJobs = taskService.getRunningJobs();
            return new TsJobResponseVO<>(200L, "处理成功", runningJobs);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get running jobs error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有已注册的任务列表
     */
    @GetMapping("/get-registered-jobs")
    public TsJobResponseVO<List<JobDTO>> getRegisteredJobs() {
        try {
            List<JobDTO> jobs = new ArrayList<>(TsJobConfig.jobs.values());
            return new TsJobResponseVO<>(200L, "处理成功", jobs);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get registered jobs error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 清除任务执行统计
     */
    @PostMapping("/clear-stats")
    public TsJobResponseVO<String> clearStats(@RequestParam String jobKey) {
        try {
            taskService.clearJobStats(jobKey);
            return new TsJobResponseVO<>(200L, "处理成功");
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] clear stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度健康状态
     */
    @GetMapping("/health")
    public TsJobResponseVO<Map<String, Object>> getHealth() {
        try {
            Map<String, Object> health = schedulerHealthService.getHealthStatus();
            return new TsJobResponseVO<>(200L, "处理成功", health);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get health error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度摘要信息
     */
    @GetMapping("/summary")
    public TsJobResponseVO<Map<String, Object>> getSummary() {
        try {
            Map<String, Object> summary = schedulerHealthService.getSchedulerSummary();
            return new TsJobResponseVO<>(200L, "处理成功", summary);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get summary error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务的执行历史记录
     */
    @GetMapping("/get-job-records")
    public TsJobResponseVO<List<TsJobRecordPO>> getJobRecords(
            @RequestParam String jobKey,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        try {
            List<TsJobRecordPO> records = tsJobRecordDAO.selectByJobKey(jobKey, limit);
            return new TsJobResponseVO<>(200L, "处理成功", records);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get job records error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取今日执行统计
     */
    @GetMapping("/get-today-stats")
    public TsJobResponseVO<Map<String, Object>> getTodayStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 今日各状态统计
            List<JobRecordBI> statusBI = tsJobRecordDAO.selectTodayRecordBI();
            stats.put("statusStats", statusBI);
            
            // 今日执行次数Top任务
            List<JobRecordBI> topJobs = tsJobRecordDAO.selectTopJobExecutions(10);
            stats.put("topJobs", topJobs);
            
            // 今日总执行次数
            long totalToday = statusBI.stream().mapToLong(JobRecordBI::getValue).sum();
            stats.put("totalExecutions", totalToday);
            
            // 计算成功率
            long successCount = statusBI.stream()
                    .filter(bi -> "1".equals(bi.getName()))
                    .mapToLong(JobRecordBI::getValue)
                    .sum();
            double successRate = totalToday > 0 ? (double) successCount / totalToday * 100 : 0;
            stats.put("successRate", successRate);
            
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get today stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取近期失败记录
     */
    @GetMapping("/get-recent-failed")
    public TsJobResponseVO<List<TsJobRecordPO>> getRecentFailed(
            @RequestParam(required = false, defaultValue = "24") int hours) {
        try {
            List<TsJobRecordPO> records = tsJobRecordDAO.selectRecentFailedRecords(hours);
            return new TsJobResponseVO<>(200L, "处理成功", records);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get recent failed records error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取慢任务记录
     */
    @GetMapping("/get-slow-records")
    public TsJobResponseVO<List<TsJobRecordPO>> getSlowRecords(
            @RequestParam(required = false, defaultValue = "30") int thresholdSeconds,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        try {
            List<TsJobRecordPO> records = tsJobRecordDAO.selectSlowRecords(thresholdSeconds, limit);
            return new TsJobResponseVO<>(200L, "处理成功", records);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get slow records error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务详细统计信息
     */
    @GetMapping("/get-job-detail-stats")
    public TsJobResponseVO<Map<String, Object>> getJobDetailStats(@RequestParam String jobKey) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 数据库统计
            TsJobRecordDAO.JobStats dbStats = tsJobRecordDAO.selectJobStats(jobKey);
            if (dbStats != null) {
                stats.put("totalCount", dbStats.getTotalCount() != null ? dbStats.getTotalCount() : 0);
                stats.put("successCount", dbStats.getSuccessCount() != null ? dbStats.getSuccessCount() : 0);
                stats.put("failCount", dbStats.getFailCount() != null ? dbStats.getFailCount() : 0);
                stats.put("avgDuration", dbStats.getAvgDuration() != null ? dbStats.getAvgDuration() : 0);
                double rate = dbStats.getTotalCount() != null && dbStats.getTotalCount() > 0 
                        ? (double) dbStats.getSuccessCount() / dbStats.getTotalCount() * 100 : 0;
                stats.put("successRate", rate);
            } else {
                stats.put("totalCount", 0);
                stats.put("successCount", 0);
                stats.put("failCount", 0);
                stats.put("avgDuration", 0);
                stats.put("successRate", 0.0);
            }
            
            // 内存统计（实时数据）
            TaskService.JobExecutionStats memStats = taskService.getJobStats(jobKey);
            if (memStats != null) {
                stats.put("inMemoryTotal", memStats.getTotalExecutions());
                stats.put("inMemorySuccess", memStats.getSuccessCount());
                stats.put("inMemoryFail", memStats.getFailureCount());
                stats.put("inMemoryAvgDuration", memStats.getAvgDuration());
                stats.put("inMemoryMaxDuration", memStats.getMaxDuration());
                stats.put("inMemoryMinDuration", memStats.getMinDuration() == Long.MAX_VALUE ? 0 : memStats.getMinDuration());
                stats.put("lastExecutionTime", memStats.getLastExecutionTime());
            }
            
            // 最近执行状态
            TsJobRecordPO lastRecord = tsJobRecordDAO.selectLastByJobKey(jobKey);
            if (lastRecord != null) {
                stats.put("lastStatus", lastRecord.getRecordStatus());
                stats.put("lastExecutionTime", lastRecord.getRecordTime());
            }
            
            // 运行状态
            stats.put("isRunning", taskService.getRunningJobCount(jobKey) > 0);
            stats.put("runningCount", taskService.getRunningJobCount(jobKey));
            
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get job detail stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    // ==================== 任务分组和标签 API ====================
    
    /**
     * 获取所有任务分组
     */
    @GetMapping("/get-job-groups")
    public TsJobResponseVO<List<String>> getJobGroups() {
        try {
            List<String> groups = taskGroupService.getAllJobGroups();
            return new TsJobResponseVO<>(200L, "处理成功", groups);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get job groups error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有任务标签
     */
    @GetMapping("/get-job-tags")
    public TsJobResponseVO<List<String>> getJobTags() {
        try {
            List<String> tags = taskGroupService.getAllTags();
            return new TsJobResponseVO<>(200L, "处理成功", tags);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get job tags error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分组获取任务列表
     */
    @GetMapping("/get-jobs-by-group")
    public TsJobResponseVO<List<JobDTO>> getJobsByGroup(String group) {
        try {
            List<JobDTO> jobs = taskGroupService.getJobsByGroup(group);
            return new TsJobResponseVO<>(200L, "处理成功", jobs);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get jobs by group error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据标签获取任务列表
     */
    @GetMapping("/get-jobs-by-tag")
    public TsJobResponseVO<List<JobDTO>> getJobsByTag(String tag) {
        try {
            List<JobDTO> jobs = taskGroupService.getJobsByTag(tag);
            return new TsJobResponseVO<>(200L, "处理成功", jobs);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get jobs by tag error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分组统计信息
     */
    @GetMapping("/get-group-stats")
    public TsJobResponseVO<Map<String, Long>> getGroupStats() {
        try {
            Map<String, Long> stats = taskGroupService.getGroupStats();
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get group stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取标签统计信息
     */
    @GetMapping("/get-tag-stats")
    public TsJobResponseVO<Map<String, Long>> getTagStats() {
        try {
            Map<String, Long> stats = taskGroupService.getTagStats();
            return new TsJobResponseVO<>(200L, "处理成功", stats);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get tag stats error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分组树结构
     */
    @GetMapping("/get-group-tree")
    public TsJobResponseVO<List<Map<String, Object>>> getGroupTree() {
        try {
            List<Map<String, Object>> tree = taskGroupService.getGroupTree();
            return new TsJobResponseVO<>(200L, "处理成功", tree);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get group tree error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取失败策略列表（前端下拉框用）
     */
    @GetMapping("/get-fail-strategies")
    public TsJobResponseVO<List<Map<String, Object>>> getFailStrategies() {
        try {
            List<Map<String, Object>> strategies = new ArrayList<>();
            
            Map<String, Object> s1 = new HashMap<>();
            s1.put("value", 1);
            s1.put("label", "停止调度");
            s1.put("desc", "任务失败后自动暂停调度");
            strategies.add(s1);
            
            Map<String, Object> s2 = new HashMap<>();
            s2.put("value", 2);
            s2.put("label", "继续重试");
            s2.put("desc", "任务失败后按重试配置继续执行（默认）");
            strategies.add(s2);
            
            Map<String, Object> s3 = new HashMap<>();
            s3.put("value", 3);
            s3.put("label", "忽略继续");
            s3.put("desc", "任务失败后记录日志但继续执行后续调度");
            strategies.add(s3);
            
            return new TsJobResponseVO<>(200L, "处理成功", strategies);
        } catch (Exception e) {
            log.error("[ts-job-TsJobApi] get fail strategies error: {}", e.getMessage(), e);
            return new TsJobResponseVO<>(500L, "处理失败: " + e.getMessage());
        }
    }
}