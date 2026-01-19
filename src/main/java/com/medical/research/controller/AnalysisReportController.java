package com.medical.research.controller;

import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/analysis")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "统计分析模块", description = "DeLong检验、AUC计算、报告生成接口")
public class AnalysisReportController {

    @Autowired
    private AnalysisReportService analysisReportService;

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @PostMapping("/generate")
    @Operation(summary = "生成分析报告", description = "生成DeLong/AUC报告及ROC图")
    public Result<Map<String, Object>> generateReport(
            @Parameter(description = "报告参数", required = true) @RequestBody AnalysisReport report) {
        try {
            experimentPlanService.checkRightExperimentPlan(report.getExperimentId());
            Map<String, Object> result = analysisReportService.generateReport(report);
            return Result.success("报告生成成功", result);
        } catch (Exception e) {
            log.error("生成失败：", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }
}