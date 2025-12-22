package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.entity.ExperimentPlan;
import com.medical.research.mapper.ExperimentPlanMapper;
import com.medical.research.service.ExperimentPlanService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 实验方案Service实现类
 */
@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan> implements ExperimentPlanService {

    @Override
    public IPage<ExperimentPlan> getPageList(String planName, Integer pageNum, Integer pageSize) {
        Page<ExperimentPlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ExperimentPlan> wrapper = new LambdaQueryWrapper<>();
        // 模糊查询实验名称
        if (planName != null && !planName.trim().isEmpty()) {
            wrapper.like(ExperimentPlan::getPlanName, planName.trim());
        }
        // 按更新时间倒序
        wrapper.orderByDesc(ExperimentPlan::getId,ExperimentPlan::getUpdateTime);
//        wrapper.orderByDesc(ExperimentPlan::getId);
        return this.page(page, wrapper); // 返回IPage类型，包含分页信息和total值
    }

    @Override
    public List<ExperimentPlan> getAllList() {
        LambdaQueryWrapper<ExperimentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExperimentPlan::getCreateTime);
        return this.list(wrapper);
    }
}