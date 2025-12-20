package com.medical.research.dto.sys;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:59
 * @Description:
 */
import lombok.Data;

@Data
public class SysUserRoleRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;
}