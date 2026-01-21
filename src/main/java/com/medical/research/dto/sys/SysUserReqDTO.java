package com.medical.research.dto.sys;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserReqDTO extends PageDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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
     * 科室
     */
    private Integer departmentCode;

    /**
     * 职称
     */
    private Integer titleCode;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
}