package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.sys.SysUserRespDTO;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.research.ResearchData;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.service.ResearchDataService;
import com.medical.research.service.SysUserService;
import com.medical.research.util.Result;
import com.medical.research.util.SecurityUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/data")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "科研数据模块", description = "科研数据导入、查询、删除接口")
public class ResearchDataController {

    @Autowired
    private ResearchDataService researchDataService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @PostMapping("/import")
    @Operation(summary = "CSV批量导入", description = "上传CSV文件关联实验ID导入数据")
    public Result<String> importCsvData(
            @Parameter(description = "实验ID", required = true) @RequestParam Long experimentId,
            @Parameter(description = "CSV文件", required = true) @RequestParam MultipartFile file) {
        try {
            experimentPlanService.checkRightExperimentPlan(experimentId);
            String res = researchDataService.importCsvData(experimentId, file);
            return Result.success("导入成功", res);
        } catch (Exception e) {
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询", description = "按实验ID分页查询科研数据")
    public Result<Page<ResearchData>> getPageList(
            @Parameter(description = "实验ID（可选）") @RequestParam(required = false) Long experimentId,
            @Parameter(description = "页码", example = "1") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数", example = "10") @RequestParam Integer pageSize) {
        try {
            String username = SecurityUserUtil.getCurrentUsername();
            SysUserRespDTO user = sysUserService.getUserByUsername(username);
            List<Long> experimentIds = new ArrayList<>();
            if (experimentId == null) {
                List<ExperimentPlan> list = experimentPlanService.getAllListByUserId(user.getId());
                experimentIds = list.stream().map(ExperimentPlan::getId).collect(Collectors.toList());
            } else {
                experimentIds.add(experimentId);
                experimentPlanService.checkRightExperimentPlan(experimentId);
            }
            Page<ResearchData> pageList = researchDataService.getPageList(experimentIds, pageNum, pageSize);
            return Result.success("查询成功", pageList);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "单条查询", description = "按数据ID查询详情")
    public Result<ResearchData> getById(
            @Parameter(description = "数据ID", required = true) @PathVariable Long id) {
        try {
            ResearchData data = researchDataService.getById(id);
            if (data == null) {
                return Result.error("数据不存在");
            }
            experimentPlanService.checkRightExperimentPlan(data.getExperimentId());
            return Result.success("查询成功", data);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "单条删除", description = "按数据ID删除数据")
    public Result<String> deleteById(
            @Parameter(description = "数据ID", required = true) @PathVariable Long id) {
        try {
            ResearchData data = researchDataService.getById(id);
            if (data == null) {
                return Result.error("数据不存在");
            }
            experimentPlanService.checkRightExperimentPlan(data.getExperimentId());
            boolean success = researchDataService.update(new UpdateWrapper<ResearchData>()
                    .set("status", ResearchData.Status.DELETED.getValue())
                    .eq("id", id));
            return success ? Result.success("删除成功") : Result.error("数据不存在");
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除", description = "按ID列表批量删除数据")
    public Result<String> batchDelete(
            @Parameter(description = "ID列表（逗号分隔）", required = true) @RequestParam String ids) {
        try {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            boolean success = researchDataService.update(new UpdateWrapper<ResearchData>()
                    .set("status", ResearchData.Status.DELETED.getValue())
                    .in("id", idList));
            return success ? Result.success("批量删除成功") : Result.error("部分数据不存在");
        } catch (Exception e) {
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }
}