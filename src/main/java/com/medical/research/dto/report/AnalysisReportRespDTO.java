// 分析报告响应DTO
package com.medical.research.dto.report;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class AnalysisReportRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 报告名称
     */
    private String reportName;

    /**
     * 实验方案ID
     */
    private Long planId;

    /**
     * 实验方案名称
     */
    private String planName;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 版本
     */
    private String version;

    /**
     * 状态：1-已生成 2-已导出 3-已作废
     */
    private Integer status;

    /**
     * 报告内容（HTML/Markdown）
     */
    private String reportContent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}