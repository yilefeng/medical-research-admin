package com.medical.research.entity.analysis;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.research.entity.BaseDO;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 分析报告表实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport extends BaseDO {

    /**
     * 关联实验ID
     */
    private Long experimentId;

    /**
     * 检验方法（DeLong/AUC)
     */
    private String testMethod;

    /**
     * 模型1 AUC
     */
    private BigDecimal auc1;

    /**
     * 模型2 AUC
     */
    private BigDecimal auc2;

    /**
     * AUC差异
     */
    private BigDecimal aucDiff;

    /**
     * 标准误
     */
    private BigDecimal stdErr;

    /**
     * Z值
     */
    private BigDecimal zValue;

    /**
     * 双侧P值
     */
    private BigDecimal pValue;

    /**
     * 报告名称
     */
    private String reportName;

    /**
     * ROC图片存储路径
     */
    private String rocImagePath;

    /**
     * 分析数据CSV文件存储路径
     */
    private String pdfPath;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 状态（1：正常，0：删除）
     */
    private Integer status;

    @Getter
    public static enum Status {
        NORMAL(1),
        DELETED(0);
        private final Integer value;
        Status(Integer value) {
            this.value = value;
        }
    }
}