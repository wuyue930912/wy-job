package com.ts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ts.dto.JobRecordBI;
import com.ts.po.TsJobRecordPO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TsJobRecordDAO extends BaseMapper<TsJobRecordPO> {

    @Select("select record_status as name, count(1) as value from ts_job_record tjr group by record_status order by record_status asc")
    List<JobRecordBI> selectRecordBI();

    /**
     * 查询任务最近一次执行记录
     * @param jobKey 任务key
     * @return 最近一次执行记录
     */
    @Select("SELECT * FROM ts_job_record WHERE job_key = #{jobKey} ORDER BY record_time DESC LIMIT 1")
    TsJobRecordPO selectLastByJobKey(String jobKey);

    /**
     * 查询任务执行历史记录
     * @param jobKey 任务key
     * @param limit 返回记录数
     * @return 执行记录列表
     */
    @Select("SELECT * FROM ts_job_record WHERE job_key = #{jobKey} ORDER BY record_time DESC LIMIT #{limit}")
    List<TsJobRecordPO> selectByJobKey(String jobKey, int limit);

    /**
     * 查询任务执行统计信息
     * @param jobKey 任务key
     * @return 统计信息
     */
    @Select("SELECT " +
            " COUNT(1) as totalCount, " +
            " SUM(CASE WHEN record_status = 1 THEN 1 ELSE 0 END) as successCount, " +
            " SUM(CASE WHEN record_status = 2 THEN 1 ELSE 0 END) as failCount, " +
            " AVG(TIMESTAMPDIFF(SECOND, record_time, end_time)) as avgDuration " +
            " FROM ts_job_record WHERE job_key = #{jobKey}")
    JobStats selectJobStats(String jobKey);
    
    /**
     * 查询今日执行记录
     * @return 今日执行记录列表
     */
    @Select("SELECT * FROM ts_job_record WHERE DATE(record_time) = CURDATE() ORDER BY record_time DESC")
    List<TsJobRecordPO> selectTodayRecords();
    
    /**
     * 查询近期失败的执行记录
     * @param hours 小时数
     * @return 失败记录列表
     */
    @Select("SELECT * FROM ts_job_record WHERE record_status = 2 AND record_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) ORDER BY record_time DESC")
    List<TsJobRecordPO> selectRecentFailedRecords(int hours);
    
    /**
     * 统计今日各状态执行数量
     * @return 统计结果
     */
    @Select("SELECT record_status as name, count(1) as value FROM ts_job_record WHERE DATE(record_time) = CURDATE() GROUP BY record_status")
    List<JobRecordBI> selectTodayRecordBI();
    
    /**
     * 查询执行时长超过阈值的时间记录（用于分析慢任务）
     * @param thresholdSeconds 阈值（秒）
     * @param limit 记录数
     * @return 慢任务记录列表
     */
    @Select("SELECT * FROM ts_job_record WHERE TIMESTAMPDIFF(SECOND, record_time, end_time) > #{thresholdSeconds} ORDER BY record_time DESC LIMIT #{limit}")
    List<TsJobRecordPO> selectSlowRecords(int thresholdSeconds, int limit);
    
    /**
     * 统计任务执行次数（按任务分组）
     * @param limit 返回数量
     * @return 统计列表
     */
    @Select("SELECT job_key as name, count(1) as value FROM ts_job_record WHERE DATE(record_time) = CURDATE() GROUP BY job_key ORDER BY value DESC LIMIT #{limit}")
    List<JobRecordBI> selectTopJobExecutions(int limit);
    
    /**
     * 获取指定时间范围内的执行统计
     * @param jobKey 任务key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Select("SELECT " +
            " COUNT(1) as totalCount, " +
            " SUM(CASE WHEN record_status = 1 THEN 1 ELSE 0 END) as successCount, " +
            " SUM(CASE WHEN record_status = 2 THEN 1 ELSE 0 END) as failCount " +
            " FROM ts_job_record WHERE job_key = #{jobKey} AND record_time BETWEEN #{startTime} AND #{endTime}")
    JobStats selectJobStatsByTimeRange(String jobKey, String startTime, String endTime);

    /**
     * 任务执行统计结果
     */
    class JobStats {
        private Long totalCount;
        private Long successCount;
        private Long failCount;
        private Double avgDuration;

        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        public Long getSuccessCount() { return successCount; }
        public void setSuccessCount(Long successCount) { this.successCount = successCount; }
        public Long getFailCount() { return failCount; }
        public void setFailCount(Long failCount) { this.failCount = failCount; }
        public Double getAvgDuration() { return avgDuration; }
        public void setAvgDuration(Double avgDuration) { this.avgDuration = avgDuration; }
    }
}