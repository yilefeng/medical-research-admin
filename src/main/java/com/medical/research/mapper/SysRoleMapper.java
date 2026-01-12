package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.sys.SysRole;
import com.medical.research.dto.sys.SysRoleReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 分页查询角色
     */
    @Select("SELECT * FROM sys_role")
    List<SysRole> selectRolePage(@Param("req") SysRoleReqDTO req);

    /**
     * 查询角色总数
     */
    @Select("SELECT COUNT(*) FROM sys_role")
    Long selectRoleCount(@Param("req") SysRoleReqDTO req);

    /**
     * 根据编码查询角色
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    SysRole selectByCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色
     */
    @Select("SELECT * FROM sys_role WHERE id IN (SELECT role_id FROM sys_user_role WHERE user_id = #{userId})")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
}