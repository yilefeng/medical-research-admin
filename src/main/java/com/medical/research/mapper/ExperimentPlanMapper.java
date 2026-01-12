package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.dto.experiment.ExperimentPlanReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExperimentPlanMapper extends BaseMapper<ExperimentPlan> {

    /**
     * 分页查询实验方案
     */
    List<ExperimentPlan> selectPlanPage(@Param("req") ExperimentPlanReqDTO req);

    /**
     * 查询实验方案总数
     */
    Long selectPlanCount(@Param("req") ExperimentPlanReqDTO req);
}