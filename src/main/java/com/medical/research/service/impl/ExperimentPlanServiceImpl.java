package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.entity.ExperimentPlan;
import com.medical.research.mapper.ExperimentPlanMapper;
import com.medical.research.dto.ExperimentPlanReqDTO;
import com.medical.research.dto.ExperimentPlanRespDTO;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.util.SecurityUserUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan>
        implements ExperimentPlanService {

    @Resource
    private ExperimentPlanMapper experimentPlanMapper;

    @Override
    public Page<ExperimentPlanRespDTO> getPlanPage(ExperimentPlanReqDTO req) {
        Page<ExperimentPlanRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = experimentPlanMapper.selectPlanCount(req);
        page.setTotal(total);

        // 查询列表
        List<ExperimentPlan> planList = experimentPlanMapper.selectPlanPage(req);
        List<ExperimentPlanRespDTO> respList = planList.stream().map(plan -> {
            ExperimentPlanRespDTO resp = new ExperimentPlanRespDTO();
            BeanUtils.copyProperties(plan, resp);
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPlan(ExperimentPlanReqDTO req) {
        ExperimentPlan plan = new ExperimentPlan();
        BeanUtils.copyProperties(req, plan);
        plan.setCreateTime(LocalDateTime.now());
        plan.setUpdateTime(LocalDateTime.now());
        plan.setUserId(SecurityUserUtil.getCurrentUserId());
        return save(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlan(ExperimentPlanReqDTO req) {
        ExperimentPlan plan = getById(req.getId());
        if (plan == null) {
            return false;
        }
        BeanUtils.copyProperties(req, plan);
        plan.setUpdateTime(LocalDateTime.now());
        return updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlanStatus(Long id, Integer status) {
        ExperimentPlan plan = getById(id);
        if (plan == null) {
            return false;
        }
        plan.setStatus(status);
        plan.setUpdateTime(LocalDateTime.now());
        return updateById(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlan(Long id) {
        return removeById(id);
    }

    @Override
    public ExperimentPlanRespDTO getPlanById(Long id) {
        ExperimentPlan plan = getById(id);
        if (plan == null) {
            return null;
        }
        ExperimentPlanRespDTO resp = new ExperimentPlanRespDTO();
        BeanUtils.copyProperties(plan, resp);
        return resp;
    }
}