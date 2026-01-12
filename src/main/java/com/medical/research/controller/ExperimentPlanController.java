package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.experiment.ExperimentPlanReqDTO;
import com.medical.research.dto.experiment.ExperimentPlanRespDTO;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.experiment.ExperimentResearcher;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.service.ExperimentResearcherService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实验方案Controller
 */
@RestController
@RequestMapping("/experiment")
@Tag(name = "实验方案模块", description = "实验方案的新增、查询、编辑、删除接口")
public class ExperimentPlanController {

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @Autowired
    private ExperimentResearcherService experimentResearcherService;

    @PostMapping("/add")
    @Operation(summary = "新增实验方案", description = "创建新的科研实验方案")
    public Result<String> addExperiment(
            @Parameter(description = "实验方案信息", required = true) @RequestBody ExperimentPlanReqDTO reqDTO) {
        try {
            ExperimentPlan experimentPlan = new ExperimentPlan();
            BeanUtils.copyProperties(reqDTO, experimentPlan);
            experimentPlan.setCreateTime(LocalDateTime.now());
            experimentPlan.setUpdateTime(LocalDateTime.now());
            boolean success = experimentPlanService.save(experimentPlan);
            if (success) {
                Long generatedId = experimentPlan.getId();
                experimentResearcherService.saveResearcher(generatedId, reqDTO.getResearchIds());
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
            @Parameter(description = "更新后的实验方案信息（含ID）", required = true) @RequestBody ExperimentPlanReqDTO reqDTO) {
        try {
            ExperimentPlan experimentPlan = new ExperimentPlan();
            BeanUtils.copyProperties(reqDTO, experimentPlan);
            experimentPlan.setCreateTime(LocalDateTime.now());
            experimentPlan.setUpdateTime(LocalDateTime.now());
            boolean success = experimentPlanService.updateById(experimentPlan);
            if (success) {
                Long generatedId = experimentPlan.getId();
                experimentResearcherService.saveResearcher(generatedId, reqDTO.getResearchIds());
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
    public Result<IPage<ExperimentPlanRespDTO>> getExperimentPageList(
            @Parameter(description = "实验名称（模糊查询，可选）") @RequestParam(required = false) String planName,
            @Parameter(description = "页码", required = true, example = "1") @RequestParam Integer pageNum,
            @Parameter(description = "每页条数", required = true, example = "10") @RequestParam Integer pageSize) {
        try {
            ExperimentPlanReqDTO reqDTO = new ExperimentPlanReqDTO();
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(pageSize);
            reqDTO.setPlanName(planName);
            IPage<ExperimentPlanRespDTO> pageList = experimentPlanService.getPageList(reqDTO);
            // 检查是否有实验记录，避免IN查询为空的情况
            if (pageList.getRecords() != null && !pageList.getRecords().isEmpty()) {
                List<Long> experimentIds = pageList.getRecords().stream()
                        .map(ExperimentPlanRespDTO::getId)
                        .collect(Collectors.toList());

                List<ExperimentResearcher> researchers = experimentResearcherService.list(
                        new QueryWrapper<ExperimentResearcher>()
                                .in("experiment_id", experimentIds)
                                .eq("status", ExperimentResearcher.Status.NORMAL.getValue())
                );

                Map<Long, List<ExperimentResearcher>> researcherMap = researchers.stream()
                        .collect(Collectors.groupingBy(ExperimentResearcher::getExperimentId));

                pageList.getRecords().forEach(plan -> {
                    List<ExperimentResearcher> experimentResearchers = researcherMap.get(plan.getId());
                    if (experimentResearchers != null) {
                        plan.setResearchIds(
                                experimentResearchers.stream().map(ExperimentResearcher::getResearcherId).collect(Collectors.toList()));
                    }
                });
            } else {
                // 如果没有实验记录，则为每个计划设置空的研究员ID列表
                pageList.getRecords().forEach(plan -> plan.setResearchIds(Collections.emptyList()));
            }

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