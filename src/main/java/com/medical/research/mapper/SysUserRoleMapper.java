package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色关联
     */
    List<SysUserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户关联
     */
    List<SysUserRole> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量删除用户角色关联
     */
    int deleteByUserId(@Param("userId") Long userId);
}