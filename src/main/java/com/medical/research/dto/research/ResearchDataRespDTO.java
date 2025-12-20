package com.medical.research.dto.research;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ResearchDataRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 实验编号
     */
    private String experimentNo;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 数据集
     */
    private String dataset;

    /**
     * 准确率
     */
    private BigDecimal accuracy;

    /**
     * 精确率
     */
    private BigDecimal precision;

    /**
     * 召回率
     */
    private BigDecimal recall;

    /**
     * F1分数
     */
    private BigDecimal f1Score;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
