package com.medical.research.controller;

import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/report")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "报告管理模块", description = "报告查询、预览、下载、删除接口")
public class ReportManageController {

    @Autowired
    private AnalysisReportService analysisReportService;

    @GetMapping("/list")
    @Operation(summary = "报告分页查询", description = "按报告名称模糊查询")
    public Result<Object> getReportList(
            @Parameter(description = "报告名称（可选）") @RequestParam(required = false) String reportName,
            @Parameter(description = "页码", example = "1") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam Integer pageSize) {
        try {
            Object data = analysisReportService.getReportPageList(reportName, pageNum, pageSize);
            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "单条报告查询", description = "按报告ID查询详情")
    public Result<AnalysisReport> getReportById(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id) {
        try {
            AnalysisReport report = analysisReportService.getById(id);
            return Result.success("查询成功", report);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview/{id}")
    @Operation(summary = "PDF预览", description = "返回PDF文件流")
    public void previewPdf(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id,
            HttpServletResponse response) throws IOException {
        try {
            analysisReportService.previewPdf(id, response);
        } catch (Exception e) {
            response.getWriter().write("预览失败：" + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "PDF下载", description = "触发浏览器下载PDF")
    public void downloadPdf(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id,
            HttpServletResponse response) throws IOException {
        try {
            analysisReportService.downloadPdf(id, response);
        } catch (Exception e) {
            response.getWriter().write("下载失败：" + e.getMessage());
        }
    }

    @GetMapping("/roc/preview/{id}")
    @Operation(summary = "ROC图片预览", description = "返回ROC图片流")
    public void previewRocImage(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id,
            HttpServletResponse response) throws IOException {
        try {
            analysisReportService.previewRocImage(id, response);
        } catch (Exception e) {
            response.getWriter().write("预览失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "报告删除", description = "删除报告及对应文件")
    public Result<String> deleteReport(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id) {
        try {
            boolean success = analysisReportService.deleteReportWithFile(id);
            return success ? Result.success("删除成功") : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}