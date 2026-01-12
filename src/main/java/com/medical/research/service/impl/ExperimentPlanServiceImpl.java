package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.dto.experiment.ExperimentPlanReqDTO;
import com.medical.research.dto.experiment.ExperimentPlanRespDTO;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.experiment.ExperimentResearcher;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.mapper.ExperimentPlanMapper;
import com.medical.research.mapper.SysUserMapper;
import com.medical.research.service.ExperimentPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验方案Service实现类
 */
@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan> implements ExperimentPlanService {

    @Resource
    private ExperimentPlanMapper experimentPlanMapper;

    @Override
    public IPage<ExperimentPlanRespDTO> getPageList(ExperimentPlanReqDTO req) {
        Page<ExperimentPlanRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());
        // 查询总数
        Long total = experimentPlanMapper.selectPlanCount(req);
        page.setTotal(total);
        // 查询列表
        List<ExperimentPlan> list = experimentPlanMapper.selectPlanPage(req);
        List<ExperimentPlanRespDTO> respList = list.stream().map(experimentPlan -> {
            ExperimentPlanRespDTO respDTO = new ExperimentPlanRespDTO();
            BeanUtils.copyProperties(experimentPlan, respDTO);
            return respDTO;
        }).collect(Collectors.toList());
        page.setRecords(respList);
        return page;
    }

    @Override
    public List<ExperimentPlan> getAllList() {
        LambdaQueryWrapper<ExperimentPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExperimentPlan::getCreateTime);
        return this.list(wrapper);
    }
}