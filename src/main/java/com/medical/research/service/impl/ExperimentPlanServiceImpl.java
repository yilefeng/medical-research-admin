package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.dto.experiment.ExperimentPlanReqDTO;
import com.medical.research.dto.experiment.ExperimentPlanRespDTO;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.sys.SysRole;
import com.medical.research.exception.BusinessException;
import com.medical.research.mapper.ExperimentPlanMapper;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.service.SysRoleService;
import com.medical.research.service.SysUserService;
import com.medical.research.util.SecurityUserUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实验方案Service实现类
 */
@Service
public class ExperimentPlanServiceImpl extends ServiceImpl<ExperimentPlanMapper, ExperimentPlan> implements ExperimentPlanService {

    @Resource
    private ExperimentPlanMapper experimentPlanMapper;

    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysUserService sysUserService;

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
        wrapper.eq(ExperimentPlan::getStatus, ExperimentPlan.Status.NORMAL.getValue());
        wrapper.orderByDesc(ExperimentPlan::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public List<ExperimentPlan> getAllListByUserId(Long userId) {
        List<SysRoleRespDTO> roles = sysRoleService.getRolesByUserId(userId);
        ExperimentPlanReqDTO req = new ExperimentPlanReqDTO();
        req.setOwnerId(roles.get(0).getRoleCode().equals(SysRole.ROLE_ADMIN_CODE) ? null : userId);
        req.setStatus(ExperimentPlan.Status.NORMAL.getValue());
        req.setPageNum(null);
        return experimentPlanMapper.selectPlanPage(req);
    }

    @Override
    public void checkRightExperimentPlan(Long planId) {
        String username = SecurityUserUtil.getCurrentUsername();
        SysUserRespDTO respDTO = sysUserService.getUserByUsername(username);
        if (!respDTO.getRoleCode().equals(SysRole.ROLE_ADMIN_CODE)) {
            Integer join = experimentPlanMapper.existJoin(planId, respDTO.getId());
            if (join == null || join == 0) {
                throw new BusinessException("没有权限");
            }
        }
    }
}