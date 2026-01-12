// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.sys.SysRole;
import com.medical.research.dto.sys.SysRoleReqDTO;
import com.medical.research.dto.sys.SysRoleRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色
     */
    Page<SysRoleRespDTO> getRolePage(SysRoleReqDTO req);

    /**
     * 新增角色
     */
    boolean addRole(SysRoleReqDTO req);

    /**
     * 修改角色
     */
    boolean updateRole(SysRoleReqDTO req);

    /**
     * 删除角色
     */
    boolean deleteRole(Long id);

    /**
     * 根据ID查询角色
     */
    SysRoleRespDTO getRoleById(Long id);

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRoleRespDTO> getRolesByUserId(Long userId);

    /**
     * 检查角色编码是否重复
     */
    SysRole getByRoleCode(String roleCode);

    /**
     * 检查角色是否被用户关联
     */
    boolean hasUserAssociated(Long roleId);
}

