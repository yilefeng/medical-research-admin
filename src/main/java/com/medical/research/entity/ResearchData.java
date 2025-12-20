package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 科研数据表实体类
 */
@Data
@TableName("research_data")
public class ResearchData {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 实验编号 */
    private String experimentNo;

    /** 模型名称 */
    private String modelName;

    /** 数据集名称 */
    private String dataset;

    /** 准确率 */
    private BigDecimal accuracy;

    /** 精确率 */
    @TableField("`precision`")
    private BigDecimal precision;

    /** 召回率 */
    private BigDecimal recall;

    /** F1分数 */
    private BigDecimal f1Score;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}