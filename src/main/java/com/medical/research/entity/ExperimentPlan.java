package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实验方案表实体类
 */
@Data
@TableName("experiment_plan")
public class ExperimentPlan {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String planName;

    private String researchPurpose;

    private String modelInfo;

    private String experimentDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}