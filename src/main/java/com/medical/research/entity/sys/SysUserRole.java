package com.medical.research.entity.sys;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户角色关联表实体类
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 角色ID */
    private Long roleId;

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