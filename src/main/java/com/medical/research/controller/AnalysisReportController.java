package com.medical.research.controller;

import com.medical.research.entity.AnalysisReport;
import com.medical.research.service.AnalysisReportService;
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

    @PostMapping("/generate")
    @Operation(summary = "生成分析报告", description = "生成DeLong/AUC报告及ROC图")
    public Result<Map<String, Object>> generateReport(
            @Parameter(description = "报告参数", required = true) @RequestBody AnalysisReport report) {
        try {
            Map<String, Object> result = analysisReportService.generateReport(report);
            return Result.success("报告生成成功", result);
        } catch (Exception e) {
            log.error("生成失败：", e);
            return Result.error("生成失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{reportId}")
    @Operation(summary = "报告详情", description = "按报告ID查询分析结果")
    public Result<Map<String, Object>> getReportDetail(
            @Parameter(description = "报告ID", required = true) @PathVariable Long reportId) {
        try {
            Map<String, Object> detail = analysisReportService.getReportDetail(reportId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/roc/data/{reportId}")
    @Operation(summary = "ROC数据", description = "按报告ID获取FPR/TPR数据")
    public Result<Map<String, Object>> getRocData(
            @Parameter(description = "报告ID", required = true) @PathVariable Long reportId) {
        try {
            Map<String, Object> rocData = analysisReportService.getRocData(reportId);
            return Result.success("查询成功", rocData);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}