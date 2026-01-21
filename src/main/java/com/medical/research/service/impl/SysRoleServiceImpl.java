package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.entity.sys.SysRole;
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
