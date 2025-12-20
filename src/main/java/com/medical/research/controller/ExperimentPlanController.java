package com.medical.research.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.research.dto.ExperimentPlanReqDTO;
import com.medical.research.dto.ExperimentPlanRespDTO;
import com.medical.research.entity.ExperimentPlan;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 实验方案控制器
 */
@RestController
@RequestMapping("/experiment/plan")
@RequiredArgsConstructor
@Tag(name = "实验方案模块", description = "实验方案的增删改查、状态更新等接口")
public class ExperimentPlanController {
    private final ExperimentPlanService experimentPlanService;

    /**
     * 分页查询实验方案
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "分页查询实验方案",
            description = "支持按实验名称、编号、状态等条件筛选，管理员可查所有，科研人员仅查本人创建的",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "planName", description = "实验方案名称（模糊查询）", example = "肺癌"),
                    @Parameter(name = "experimentNo", description = "实验编号", example = "EXP2025001"),
                    @Parameter(name = "status", description = "状态（0-未开始，1-进行中，2-已完成，3-已终止）", example = "1")
            }
    )
    public Result<IPage<ExperimentPlanRespDTO>> getPlanPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String experimentNo,
            @RequestParam(required = false) Integer status) {
        ExperimentPlanReqDTO req = new ExperimentPlanReqDTO();
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);
        req.setPlanName(planName);
        req.setExperimentNo(experimentNo);
        req.setStatus(status);
        IPage<ExperimentPlanRespDTO> result = experimentPlanService.getPlanPage(req);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询实验方案详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "查询实验方案详情",
            description = "根据ID查询实验方案完整信息",
            parameters = {@Parameter(name = "id", description = "实验方案ID", required = true, example = "1")}
    )
    public Result<ExperimentPlan> getPlanById(@PathVariable Long id) {
        ExperimentPlan plan = experimentPlanService.getById(id);
        if (plan == null) {
            return Result.error("实验方案不存在");
        }
        return Result.success("查询成功", plan);
    }

    /**
     * 新增实验方案
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "新增实验方案",
            description = "创建新的实验方案，关联用户ID为当前登录用户"
    )
    public Result<?> createPlan(@Valid @RequestBody ExperimentPlanReqDTO planDTO) {
        boolean success = experimentPlanService.addPlan(planDTO);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改实验方案
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "修改实验方案",
            description = "根据ID更新实验方案信息，仅可修改未开始/进行中的方案",
            parameters = {@Parameter(name = "id", description = "实验方案ID", required = true, example = "1")}
    )
    public Result<?> updatePlan(@PathVariable Long id, @Valid @RequestBody ExperimentPlanReqDTO planDTO) {
        boolean success = experimentPlanService.updatePlan(planDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除实验方案
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "删除实验方案",
            description = "管理员专属：删除实验方案（级联删除关联数据）",
            parameters = {@Parameter(name = "id", description = "实验方案ID", required = true, example = "1")}
    )
    public Result<?> deletePlan(@PathVariable Long id) {
        boolean success = experimentPlanService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 更新实验方案状态
     */
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "更新实验状态",
            description = "更新实验方案的状态（0-未开始，1-进行中，2-已完成，3-已终止）",
            parameters = {
                    @Parameter(name = "id", description = "实验方案ID", required = true, example = "1"),
                    @Parameter(name = "status", description = "目标状态", required = true, example = "2")
            }
    )
    public Result<?> updatePlanStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = experimentPlanService.updatePlanStatus(id, status);
        return success ? Result.success("状态更新成功") : Result.error("状态更新失败");
    }
}