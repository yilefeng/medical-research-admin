package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.medical.research.entity.dit.Department;
import com.medical.research.mapper.DepartmentMapper;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/21 19:04
 * @Description:
 */
@RestController
@RequestMapping("/dit/department")
@RequiredArgsConstructor
@Tag(name = "科室模块", description = "科室信息")
public class DepartmentController {
    private final DepartmentMapper departmentMapper;

    @GetMapping("/list")
    @Operation(summary = "获取科室数据", description = "获取所有科室")
    public Result<?> queryList() {
        return Result.success("查询成功", departmentMapper.selectList(new QueryWrapper<Department>().eq("status", 1)));
    }
}
