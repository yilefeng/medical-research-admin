package com.medical.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.dto.experiment.ExperimentPlanReqDTO;
import com.medical.research.dto.experiment.ExperimentPlanRespDTO;
import com.medical.research.entity.experiment.ExperimentPlan;

import java.util.List;


public interface ExperimentPlanService extends IService<ExperimentPlan> {

    /**
     * 分页查询实验方案
     * @return 分页结果
     */
    IPage<ExperimentPlanRespDTO> getPageList(ExperimentPlanReqDTO req);

    /**
     * 查询所有实验方案（下拉框使用）
     * @return 实验方案列表
     */
    List<ExperimentPlan> getAllList();
}