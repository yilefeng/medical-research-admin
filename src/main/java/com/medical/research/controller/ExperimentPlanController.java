package com.medical.research.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实验方案Controller
 */
@RestController
@RequestMapping("/experiment")
@Tag(name = "实验方案模块", description = "实验方案的新增、查询、编辑、删除接口")
public class ExperimentPlanController {

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @PostMapping("/add")
    @Operation(summary = "新增实验方案", description = "创建新的科研实验方案")
    public Result<String> addExperiment(
            @Parameter(description = "实验方案信息", required = true) @RequestBody ExperimentPlan experimentPlan) {
        try {
            experimentPlan.setCreateTime(LocalDateTime.now());
            experimentPlan.setUpdateTime(LocalDateTime.now());
            boolean success = experimentPlanService.save(experimentPlan);
            if (success) {
                return Result.success("实验方案新增成功");
            } else {
                return Result.error("实验方案新增失败");
            }
        } catch (Exception e) {
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/update")
    @Operation(summary = "编辑实验方案", description = "修改已有实验方案信息")
    public Result<String> updateExperiment(
            @Parameter(description = "更新后的实验方案信息（含ID）", required = true) @RequestBody ExperimentPlan experimentPlan) {
        try {
            boolean success = experimentPlanService.updateById(experimentPlan);
            if (success) {
                return Result.success("实验方案编辑成功");
            } else {
                return Result.error("实验方案编辑失败（数据不存在）");
            }
        } catch (Exception e) {
            return Result.error("编辑失败：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "实验方案分页查询", description = "按实验名称模糊查询，分页返回结果")
    public Result<IPage<ExperimentPlan>> getExperimentPageList(
            @Parameter(description = "实验名称（模糊查询，可选）") @RequestParam(required = false) String planName,
            @Parameter(description = "页码", required = true, example = "1") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数", required = true, example = "10") @RequestParam Integer pageSize) {
        try {
            IPage<ExperimentPlan> pageList = experimentPlanService.getPageList(planName, pageNum, pageSize);
            return Result.success("查询成功", pageList);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有实验方案", description = "返回所有实验方案（用于前端下拉框选择）")
    public Result<List<ExperimentPlan>> getAllExperimentList() {
        try {
            List<ExperimentPlan> list = experimentPlanService.getAllList();
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "按ID查询实验方案", description = "通过实验ID查询详情")
    public Result<ExperimentPlan> getExperimentById(
            @Parameter(description = "实验方案ID", required = true) @PathVariable Long id) {
        try {
            ExperimentPlan experimentPlan = experimentPlanService.getById(id);
            if (experimentPlan != null) {
                return Result.success("查询成功", experimentPlan);
            } else {
                return Result.error("实验方案不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除实验方案", description = "通过实验ID删除方案（需确保无关联科研数据）")
    public Result<String> deleteExperiment(
            @Parameter(description = "实验方案ID", required = true) @PathVariable Long id) {
        try {
            boolean success = experimentPlanService.removeById(id);
            if (success) {
                return Result.success("实验方案删除成功");
            } else {
                return Result.error("实验方案删除失败（数据不存在或存在关联数据）");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}