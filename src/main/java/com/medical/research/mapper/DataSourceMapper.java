package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.DataSource;
import com.medical.research.dto.datasource.DataSourceReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataSourceMapper extends BaseMapper<DataSource> {

    /**
     * 分页查询数据源
     */
    List<DataSource> selectSourcePage(@Param("req") DataSourceReqDTO req);

    /**
     * 查询数据源总数
     */
    Long selectSourceCount(@Param("req") DataSourceReqDTO req);
}