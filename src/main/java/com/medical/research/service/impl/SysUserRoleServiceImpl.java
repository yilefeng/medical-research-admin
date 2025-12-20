package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.entity.SysUserRole;
import com.medical.research.entity.SysRole;
import com.medical.research.mapper.SysRoleMapper;
import com.medical.research.mapper.SysUserRoleMapper;
import com.medical.research.dto.sys.SysUserRoleReqDTO;
import com.medical.research.dto.sys.SysUserRoleRespDTO;
import com.medical.research.service.SysUserRoleService;
import com.medical.research.service.SysRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 16:00
 * @Description:
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
        implements SysUserRoleService {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(SysUserRoleReqDTO req) {
        // 1. 删除用户原有角色关联
        sysUserRoleMapper.deleteByUserId(req.getUserId());

        // 2. 批量新增角色关联
        if (req.getRoleIds() == null || req.getRoleIds().isEmpty()) {
            return true;
        }

        List<SysUserRole> userRoleList = new ArrayList<>();
        for (Long roleId : req.getRoleIds()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(req.getUserId());
            userRole.setRoleId(roleId);
            userRoleList.add(userRole);
        }

        return saveBatch(userRoleList);
    }

    @Override
    public List<SysUserRoleRespDTO> getRolesByUserId(Long userId) {
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectByUserId(userId);
        List<SysUserRoleRespDTO> respList = new ArrayList<>();

        for (SysUserRole userRole : userRoleList) {
            SysUserRoleRespDTO resp = new SysUserRoleRespDTO();
            BeanUtils.copyProperties(userRole, resp);
            SysRole role = sysRoleMapper.selectById(userRole.getId());
            if (role != null) {
                resp.setRoleName(role.getRoleName());
            }

            respList.add(resp);
        }

        return respList;
    }

    @Override
    public boolean checkRoleUsed(Long roleId) {
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectByRoleId(roleId);
        return userRoleList != null && !userRoleList.isEmpty();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByUserId(Long userId) {
        int count = sysUserRoleMapper.deleteByUserId(userId);
        return count > 0;
    }
}
