package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.entity.SysRole;
import com.medical.research.mapper.SysRoleMapper;
import com.medical.research.dto.sys.SysRoleReqDTO;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.medical.research.service.SysRoleService;
import com.medical.research.service.SysUserRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:58
 * @Description:
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysUserRoleService sysUserRoleService;

    @Override
    public Page<SysRoleRespDTO> getRolePage(SysRoleReqDTO req) {
        Page<SysRoleRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = sysRoleMapper.selectRoleCount(req);
        page.setTotal(total);

        // 查询列表
        List<SysRole> roleList = sysRoleMapper.selectRolePage(req);
        List<SysRoleRespDTO> respList = roleList.stream().map(role -> {
            SysRoleRespDTO resp = new SysRoleRespDTO();
            BeanUtils.copyProperties(role, resp);
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(SysRoleReqDTO req) {
        // 检查角色编码是否重复
        SysRole existRole = sysRoleMapper.selectByCode(req.getRoleCode());
        if (existRole != null) {
            return false;
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(req, role);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());

        return save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRoleReqDTO req) {
        // 系统内置角色（1-管理员，2-科研人员）保护
        if (req.getId() == 1L || req.getId() == 2L) {
            // 仅允许修改名称和描述
            SysRole role = getById(req.getId());
            role.setRoleName(req.getRoleName());
            role.setDescription(req.getDescription());
            role.setUpdateTime(LocalDateTime.now());
            return updateById(role);
        }

        SysRole role = getById(req.getId());
        if (role == null) {
            return false;
        }

        BeanUtils.copyProperties(req, role);
        role.setUpdateTime(LocalDateTime.now());
        return updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        // 系统内置角色禁止删除
        if (id == 1L || id == 2L) {
            return false;
        }

        // 检查角色是否关联用户
        boolean hasUser = sysUserRoleService.checkRoleUsed(id);
        if (hasUser) {
            return false;
        }

        return removeById(id);
    }

    @Override
    public SysRoleRespDTO getRoleById(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            return null;
        }

        SysRoleRespDTO resp = new SysRoleRespDTO();
        BeanUtils.copyProperties(role, resp);
        return resp;
    }

    @Override
    public List<SysRoleRespDTO> getRolesByUserId(Long userId) {
        List<SysRole> roleList = sysRoleMapper.selectByUserId(userId);
        return roleList.stream().map(role -> {
            SysRoleRespDTO resp = new SysRoleRespDTO();
            BeanUtils.copyProperties(role, resp);
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    public SysRole getByRoleCode(String roleCode) {
        return sysRoleMapper.selectByCode(roleCode);
    }

    @Override
    public boolean hasUserAssociated(Long roleId) {
        return sysUserRoleService.checkRoleUsed(roleId);
    }
}
