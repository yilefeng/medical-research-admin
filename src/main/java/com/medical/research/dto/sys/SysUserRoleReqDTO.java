package com.medical.research.dto.sys;

import lombok.Data;

import java.util.List;

@Data
public class SysUserRoleReqDTO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}

