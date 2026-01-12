package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.research.ResearchData;
import com.medical.research.dto.research.ResearchDataReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResearchDataMapper extends BaseMapper<ResearchData> {

    /**
     * 分页查询科研数据
     */
    List<ResearchData> selectDataPage(@Param("req") ResearchDataReqDTO req);

    /**
     * 查询科研数据总数
     */
    Long selectDataCount(@Param("req") ResearchDataReqDTO req);

    /**
     * 根据实验编号查询数据
     */
    List<ResearchData> selectDataByExperimentNo(@Param("experimentNo") String experimentNo);
}