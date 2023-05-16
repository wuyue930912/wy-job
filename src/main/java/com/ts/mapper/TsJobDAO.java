package com.ts.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ts.po.TsJobPO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TsJobDAO  extends BaseMapper<TsJobPO> {

    @Select("select * from ts_job")
    List<TsJobPO> selectAll();

    @Select("select * from ts_job where job_key = #{key}")
    TsJobPO selectByKey(String key);

    @Select("select * from ts_job  order by create_time limit #{limit} offset #{offset}")
    List<TsJobPO> selectByPage(Integer offset, Integer limit);

    @Select("select count(1) from ts_job ")
    Long selectCountByPage();
}
