package com.medical.research.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.medical.research.entity.dit.Title;
import com.medical.research.mapper.TitleMapper;
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
@RequestMapping("/dit/title")
@RequiredArgsConstructor
@Tag(name = "职称模块", description = "职称信息")
public class TitleController {
    private final TitleMapper titleMapper;

    @GetMapping("/list")
    @Operation(summary = "职称数据", description = "获取所有职称")
    public Result<?> list() {
        return Result.success("查询成功", titleMapper.selectList(new QueryWrapper<Title>().eq("status", 1)));
    }
}
