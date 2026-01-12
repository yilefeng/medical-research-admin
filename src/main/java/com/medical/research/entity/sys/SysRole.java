package com.medical.research.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 系统角色表实体类
 */
@Data
@TableName("sys_role")
public class SysRole {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称 */
    private String roleName;

    /** 角色编码（唯一） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

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