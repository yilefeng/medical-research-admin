package com.medical.research.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 系统用户表实体类
 */
@Data
@TableName("sys_user")
public class SysUser {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（唯一） */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Getter
    public static enum Status {
        ENABLED(1, "启用"),
        DISABLED(0, "禁用");
        private final Integer code;
        private final String message;
        Status(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}