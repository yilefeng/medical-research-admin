package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.dto.sys.SysUserReqDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     */
    Page<SysUserRespDTO> getUserPage(SysUserReqDTO req);

    /**
     * 新增用户
     */
    boolean addUser(SysUserReqDTO req);

    /**
     * 修改用户
     */
    boolean updateUser(SysUserReqDTO req);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);

    /**
     * 根据ID查询用户
     */
    SysUserRespDTO getUserById(Long id);

    /**
     * 根据用户名查询用户
     */
    SysUserRespDTO getUserByUsername(String username);

    /**
     * 重置用户密码
     */
    boolean resetPassword(Long id);

    /**
     * 更新最后登录时间
     */
    boolean updateLastLoginTime(Long id);
}