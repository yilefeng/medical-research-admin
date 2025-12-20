package com.medical.research.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.report.AnalysisReportReqDTO;
import com.medical.research.dto.report.AnalysisReportRespDTO;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * 分析报告控制器
 * 核心逻辑：生成报告时通过实验方案ID关联research_data表的真实科研数据
 */
@RestController
@RequestMapping("/analysis/report")
@Tag(name = "分析报告管理", description = "分析报告的分页查询、生成、详情、导出、作废等接口")
@Slf4j
public class AnalysisReportController {

    @Autowired
    private AnalysisReportService analysisReportService;

    /**
     * 分页查询分析报告
     */
    @GetMapping("/page")
    @Operation(
            summary = "分页查询分析报告",
            description = "根据报告名称、实验方案ID、模型ID等条件分页查询分析报告列表",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = Result.class)))
            }
    )
    public Result<Page<AnalysisReportRespDTO>> getReportPage(
            @Parameter(description = "查询条件（含分页参数、筛选条件）", required = true)
            AnalysisReportReqDTO req) {
        try {
            Page<AnalysisReportRespDTO> page = analysisReportService.getReportPage(req);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("分页查询分析报告失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 生成分析报告（核心接口，关联research_data表）
     */
    @PostMapping("/generate")
    @Operation(
            summary = "生成分析报告",
            description = "基于research_data表的真实科研数据，按指定统计模型生成分析报告",
            responses = {
                    @ApiResponse(responseCode = "200", description = "生成成功",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "500", description = "生成失败",
                            content = @Content(schema = @Schema(implementation = Result.class)))
            }
    )
    public Result<Boolean> generateReport(
            @Parameter(description = "报告生成参数（planId、modelId、reportName为必传）", required = true)
            @RequestBody AnalysisReportReqDTO req) {
        // 1. 参数校验
        if (req.getPlanId() == null) {
            return Result.error("实验方案ID不能为空");
        }
        if (req.getModelId() == null) {
            return Result.error("统计模型ID不能为空");
        }
        if (!StringUtils.hasText(req.getReportName())) {
            return Result.error("报告名称不能为空");
        }

        try {
            // 2. 调用Service生成报告（Service内部会查询research_data表）
            boolean success = analysisReportService.generateReport(req);
            if (success) {
                return Result.success("报告生成成功", true);
            } else {
                return Result.error("报告生成失败");
            }
        } catch (RuntimeException e) {
            log.error("生成分析报告失败（业务异常）", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("生成分析报告失败", e);
            return Result.error("报告生成失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询报告详情
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "查询报告详情",
            description = "根据报告ID查询详情，包含基于research_data计算的统计内容",
            parameters = {
                    @Parameter(name = "id", description = "报告主键ID", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "404", description = "报告不存在",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "500", description = "查询失败",
                            content = @Content(schema = @Schema(implementation = Result.class)))
            }
    )
    public Result<AnalysisReportRespDTO> getReportById(@PathVariable Long id) {
        try {
            AnalysisReportRespDTO report = analysisReportService.getReportById(id);
            if (report == null) {
                return Result.error("报告不存在");
            }
            return Result.success("查询成功", report);
        } catch (Exception e) {
            log.error("查询报告详情失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 导出PDF报告
     */
    /**
     * 导出PDF报告
     */
    @GetMapping("/export/pdf/{id}")
    @Operation(
            summary = "导出PDF报告",
            description = "根据报告ID导出PDF格式的分析报告",
            parameters = {
                    @Parameter(name = "id", description = "报告主键ID", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "导出成功（返回PDF文件流）"),
                    @ApiResponse(responseCode = "404", description = "报告不存在或已作废"),
                    @ApiResponse(responseCode = "500", description = "导出失败")
            }
    )
    public void exportPdf(@PathVariable Long id, HttpServletResponse response) {
        try {
            // 1. 调用Service生成PDF路径
            String pdfPath = analysisReportService.exportPdf(id);
            if (!StringUtils.hasText(pdfPath)) {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(Result.error("PDF导出失败：报告不存在或已作废").toString());
                return;
            }

            // 2. 读取PDF文件并写入响应流
            try (FileInputStream fis = new FileInputStream(pdfPath);
                 OutputStream os = response.getOutputStream()) {

                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition",
                        "attachment; filename=" + URLEncoder.encode("分析报告_" + id + ".pdf", "UTF-8"));

                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
        } catch (IOException e) {
            log.error("PDF导出IO异常", e);
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(Result.error("PDF导出失败：" + e.getMessage()).toString());
            } catch (IOException ex) {
                log.error("响应写入失败", ex);
            }
        }
    }

    /**
     * 作废分析报告
     */
    @PutMapping("/invalid/{id}")
    @Operation(
            summary = "作废分析报告",
            description = "将指定ID的报告状态改为“已作废”，不删除数据",
            parameters = {
                    @Parameter(name = "id", description = "报告主键ID", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "作废成功",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "404", description = "报告不存在",
                            content = @Content(schema = @Schema(implementation = Result.class))),
                    @ApiResponse(responseCode = "500", description = "作废失败",
                            content = @Content(schema = @Schema(implementation = Result.class)))
            }
    )
    public Result<Boolean> invalidReport(@PathVariable Long id) {
        try {
            boolean success = analysisReportService.invalidReport(id);
            if (success) {
                return Result.success("报告已作废", true);
            } else {
                return Result.error("作废失败：报告不存在");
            }
        } catch (Exception e) {
            log.error("作废报告失败", e);
            return Result.error("作废失败：" + e.getMessage());
        }
    }
}