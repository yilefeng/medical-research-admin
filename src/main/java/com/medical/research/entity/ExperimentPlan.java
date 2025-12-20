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
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验方案名称 */
    private String planName;

    /** 关联实验编号 */
    private String experimentNo;

    /** 实验目的 */
    private String purpose;

    /** 实验负责人 */
    private String principal;

    /** 所属科室 */
    private String dept;

    /** 实验开始时间 */
    private LocalDateTime startTime;

    /** 实验结束时间 */
    private LocalDateTime endTime;

    /** 状态：0-未开始，1-进行中，2-已完成，3-已终止 */
    private Integer status;

    /** 创建人ID */
    private Long userId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}