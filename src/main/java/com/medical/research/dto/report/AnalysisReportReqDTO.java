// 分析报告请求DTO
package com.medical.research.dto.report;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnalysisReportReqDTO extends PageDTO {
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
     * 模型ID
     */
    private Long modelId;

    /**
     * 版本
     */
    private String version;

    /**
     * 状态：1-已生成 2-已导出 3-已作废
     */
    private Integer status;

    /**
     * 统计条件（JSON格式）
     */
    private String statConditions;

    /**
     * 报告内容（HTML/Markdown）
     */
    private String reportContent;
}