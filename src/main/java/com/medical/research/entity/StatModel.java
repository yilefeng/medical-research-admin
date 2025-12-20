package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统计模型表实体类
 */
@Data
@TableName("stat_model")
public class StatModel {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型名称 */
    private String modelName;

    /** 模型编码（唯一） */
    private String modelCode;

    /** 模型描述 */
    private String description;

    /** 模型参数配置（JSON） */
    private String params;

    /** 状态：1-可用，0-不可用 */
    private Integer status;

    /** 排序权重 */
    private Integer sort;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}