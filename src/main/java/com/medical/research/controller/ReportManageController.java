package com.medical.research.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.report.AnalysisReportRespDTO;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.service.SysUserService;
import com.medical.research.util.Result;
import com.medical.research.util.SecurityUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/report")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "报告管理模块", description = "报告查询、预览、下载、删除接口")
public class ReportManageController {

    @Autowired
    private AnalysisReportService analysisReportService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @GetMapping("/list")
    @Operation(summary = "报告分页查询", description = "按报告名称模糊查询")
    public Result<Page<AnalysisReportRespDTO>> getReportList(
            @Parameter(description = "报告名称（可选）") @RequestParam(required = false) String reportName,
            @Parameter(description = "页码", example = "1") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam Integer pageSize) {
        try {
            String username = SecurityUserUtil.getCurrentUsername();
            SysUserRespDTO user = sysUserService.getUserByUsername(username);
            List<ExperimentPlan> list = experimentPlanService.getAllListByUserId(user.getId());
            List<Long> experimentIds = list.stream().map(ExperimentPlan::getId).collect(Collectors.toList());
            Page<AnalysisReport> page = analysisReportService.getReportPageList(experimentIds, reportName, pageNum, pageSize);

            Page<AnalysisReportRespDTO> dtoPage = new Page<>();
            dtoPage.setCurrent(page.getCurrent());
            dtoPage.setSize(page.getSize());
            dtoPage.setTotal(page.getTotal());
            dtoPage.setPages(page.getPages());

            Map<Long, ExperimentPlan> planMap = list.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            ExperimentPlan::getId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));

            List<AnalysisReportRespDTO> dtoRecords = page.getRecords().stream()
                    .map(report -> {
                        AnalysisReportRespDTO resp = new AnalysisReportRespDTO();
                        try {
                            BeanUtils.copyProperties(report, resp);

                            // 设置关联的计划名称
                            Long experimentId = report.getExperimentId();
                            if (experimentId != null && planMap.containsKey(experimentId)) {
                                resp.setPlanName(planMap.get(experimentId).getPlanName());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("对象属性复制失败", e);
                        }
                        return resp;
                    })
                    .collect(Collectors.toList());

            dtoPage.setRecords(dtoRecords);
            return Result.success("查询成功", dtoPage);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "报告删除", description = "删除报告及对应文件")
    public Result<String> deleteReport(
            @Parameter(description = "报告ID", required = true) @PathVariable Long id) {
        try {
            analysisReportService.checkReport(id);
            boolean success = analysisReportService.deleteReportWithFile(id);
            return success ? Result.success("删除成功") : Result.error("报告不存在");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}