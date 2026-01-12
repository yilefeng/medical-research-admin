package com.medical.research.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.experiment.ExperimentPlan;

import java.util.List;


public interface ExperimentPlanService extends IService<ExperimentPlan> {

    /**
     * 分页查询实验方案
     * @param planName 实验名称（模糊查询）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<ExperimentPlan> getPageList(String planName, Integer pageNum, Integer pageSize);

    /**
     * 查询所有实验方案（下拉框使用）
     * @return 实验方案列表
     */
    List<ExperimentPlan> getAllList();
}