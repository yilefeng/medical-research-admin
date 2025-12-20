// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.SysUserRole;
import com.medical.research.dto.sys.SysUserRoleReqDTO;
import com.medical.research.dto.sys.SysUserRoleRespDTO;

import java.util.List;

public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 为用户分配角色
     */
    boolean assignRoles(SysUserRoleReqDTO req);

    /**
     * 根据用户ID查询角色关联
     */
    List<SysUserRoleRespDTO> getRolesByUserId(Long userId);

    /**
     * 检查角色是否被使用
     */
    boolean checkRoleUsed(Long roleId);

    /**
     * 删除用户的角色关联
     */
    boolean deleteByUserId(Long userId);

}

