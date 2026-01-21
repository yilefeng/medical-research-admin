package com.medical.research.entity.dit;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/21 19:01
 * @Description:
 */
@Data
@TableName("dit_department")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("status")
    private Integer status; // 状态（1：启用，0：禁用）

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}