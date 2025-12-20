package com.medical.research.dto.sys;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SysUserRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 角色Code
     */
    private String roleCode;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}