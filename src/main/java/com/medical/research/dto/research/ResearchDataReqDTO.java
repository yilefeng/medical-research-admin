package com.medical.research.dto.research;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ResearchDataReqDTO extends PageDTO {
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
}
