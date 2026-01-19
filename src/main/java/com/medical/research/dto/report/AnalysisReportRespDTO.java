// 分析报告响应DTO
package com.medical.research.dto.report;

import com.medical.research.entity.analysis.AnalysisReport;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnalysisReportRespDTO extends AnalysisReport {
   private String planName;
}