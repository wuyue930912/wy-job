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

}
