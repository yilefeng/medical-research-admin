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
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long experimentId;

    private Integer trueLabel;

    private Double model1Score;

    private Double model2Score;

    private String dataSource;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}