package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.mapper.SysUserMapper;
import com.medical.research.dto.sys.SysUserReqDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.service.SysUserService;
import com.medical.research.service.SysRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysRoleService sysRoleService;

    // 密码加密器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Page<SysUserRespDTO> getUserPage(SysUserReqDTO req) {
        Page<SysUserRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = sysUserMapper.selectUserCount(req);
        page.setTotal(total);

        // 查询列表
        List<SysUser> userList = sysUserMapper.selectUserPage(req);
        List<SysUserRespDTO> respList = userList.stream().map(user -> {
            SysUserRespDTO resp = new SysUserRespDTO();
            BeanUtils.copyProperties(user, resp);

            // 查询角色名称
            List<SysRoleRespDTO> roles = sysRoleService.getRolesByUserId(user.getId());
            if (roles != null) {
                resp.setRoleCode(roles.get(0).getRoleCode());
                resp.setRoleId(roles.get(0).getId());
            }

            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(SysUserReqDTO req) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserMapper.selectUserByUsername(req.getUsername());
        if (existUser != null) {
            return false;
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(req, user);

        // 密码加密
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUserReqDTO req) {
        SysUser user = getById(req.getId());
        if (user == null) {
            return false;
        }

        // 不更新密码
        BeanUtils.copyProperties(req, user, "password");
        user.setUpdateTime(LocalDateTime.now());

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 管理员账号禁止删除
        if (id == 1L) {
            return false;
        }
        return removeById(id);
    }

    @Override
    public SysUserRespDTO getUserById(Long id) {
        SysUser user = getById(id);
        return getSysUserRespDTO(user);
    }

    @Override
    public SysUserRespDTO getUserByUsername(String username) {
        SysUser user = sysUserMapper.selectUserByUsername(username);
        return getSysUserRespDTO(user);
    }


    private SysUserRespDTO getSysUserRespDTO(SysUser user) {
        if (user == null) {
            return null;
        }

        SysUserRespDTO resp = new SysUserRespDTO();
        BeanUtils.copyProperties(user, resp);
        // 查询角色名称
        List<SysRoleRespDTO> role = sysRoleService.getRolesByUserId(user.getId());
        if (role != null) {
            resp.setRoleCode(role.get(0).getRoleCode());
        }
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id) {
        // 管理员账号禁止重置
        if (id == 1L) {
            return false;
        }

        SysUser user = getById(id);
        if (user == null) {
            return false;
        }

        // 重置为123456
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdateTime(LocalDateTime.now());

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginTime(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            return false;
        }

        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setLastLoginTime(LocalDateTime.now());
        updateUser.setUpdateTime(LocalDateTime.now());
        return updateById(updateUser);
    }
}