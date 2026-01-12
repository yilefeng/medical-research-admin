package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.sys.SysUser;
import com.medical.research.dto.sys.SysUserReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户
     */
    List<SysUser> selectUserPage(@Param("req") SysUserReqDTO req);

    /**
     * 查询用户总数
     */
    Long selectUserCount(@Param("req") SysUserReqDTO req);

    /**
     * 根据用户名查询用户
     */
    SysUser selectUserByUsername(@Param("username") String username);
}