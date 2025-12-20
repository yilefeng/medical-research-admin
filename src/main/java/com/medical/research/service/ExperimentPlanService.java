package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.ExperimentPlan;
import com.medical.research.dto.ExperimentPlanReqDTO;
import com.medical.research.dto.ExperimentPlanRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ExperimentPlanService extends IService<ExperimentPlan> {

    /**
     * 分页查询实验方案
     */
    Page<ExperimentPlanRespDTO> getPlanPage(ExperimentPlanReqDTO req);

    /**
     * 新增实验方案
     */
    boolean addPlan(ExperimentPlanReqDTO req);

    /**
     * 修改实验方案
     */
    boolean updatePlan(ExperimentPlanReqDTO req);

    /**
     * 修改实验状态
     */
    boolean updatePlanStatus(Long id, Integer status);

    /**
     * 删除实验方案
     */
    boolean deletePlan(Long id);

    /**
     * 根据ID查询实验方案
     */
    ExperimentPlanRespDTO getPlanById(Long id);
}