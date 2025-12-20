package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析报告表实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long experimentId;

    private String dataIds;

    private String testMethod;

    private BigDecimal auc1;

    private BigDecimal auc2;

    private BigDecimal aucDiff;

    private BigDecimal stdErr;

    private BigDecimal zValue;

    private BigDecimal pValue;

    private String reportName;

    private String rocImagePath;

    private String pdfPath;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}