package com.medical.research.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import com.medical.research.dto.sys.SysOperLogRespDTO;
import com.medical.research.entity.SysOperLog;
import com.medical.research.service.SysOperLogService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统操作日志控制器
 */
@RestController
@RequestMapping("/sys/oper/log")
@RequiredArgsConstructor
@Tag(name = "操作日志模块", description = "系统操作日志查询（仅管理员可操作）")
public class SysOperLogController {
    private final SysOperLogService sysOperLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "分页查询操作日志",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "username", description = "操作用户名", example = "admin"),
                    @Parameter(name = "operModule", description = "操作模块", example = "ResearchDataController"),
                    @Parameter(name = "operType", description = "操作类型（新增/修改/删除/查询/导入/导出）", example = "新增")
            }
    )
    public Result<IPage<SysOperLogRespDTO>> getOperLogPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operModule,
            @RequestParam(required = false) String operType) {

        SysOperLogReqDTO req = new SysOperLogReqDTO();
        req.setUsername(username);
        req.setOperModule(operModule);
        req.setOperType(operType);
        req.setPageNum(pageNum);
        req.setPageSize(pageSize);

        IPage<SysOperLogRespDTO> result = sysOperLogService.getOperLogPage(req);
        return Result.success("查询成功", result);
    }
}