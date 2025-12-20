package com.medical.research.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.dto.datasource.DataSourceReqDTO;
import com.medical.research.dto.datasource.DataSourceRespDTO;
import com.medical.research.dto.datasource.DbTestReqDTO;
import com.medical.research.entity.DataSource;
import com.medical.research.service.DataSourceService;
import com.medical.research.util.MinioUtil;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/data/source")
@RequiredArgsConstructor
@Tag(name = "数据源模块", description = "数据源配置、文件上传、连接测试等接口")
public class DataSourceController {
    private final DataSourceService dataSourceService;
    private final MinioUtil minioUtil;

    /**
     * 分页查询数据源
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "分页查询数据源",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "sourceName", description = "数据源名称（模糊）", example = "LungCT"),
                    @Parameter(name = "sourceType", description = "数据源类型（Excel/数据库/接口）", example = "Excel")
            }
    )
    public Result<IPage<DataSourceRespDTO>> getSourcePage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) String sourceType) {
        DataSourceReqDTO reqDTO = new DataSourceReqDTO();
        reqDTO.setPageNum(pageNum);
        reqDTO.setPageSize(pageSize);
        reqDTO.setSourceName(sourceName);
        reqDTO.setSourceType(sourceType);
        IPage<DataSourceRespDTO> result = dataSourceService.getSourcePage(reqDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询数据源
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "查询数据源详情",
            parameters = {@Parameter(name = "id", description = "数据源ID", required = true, example = "1")}
    )
    public Result<DataSource> getSourceById(@PathVariable Long id) {
        DataSource source = dataSourceService.getById(id);
        if (source == null) {
            return Result.error("数据源不存在");
        }
        return Result.success("查询成功", source);
    }

    /**
     * 新增数据源（Excel）
     */
    @PostMapping("/excel")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "新增Excel数据源",
            description = "上传Excel文件并创建数据源配置"
    )
    public Result<?> createExcelSource(
            @RequestParam("file") MultipartFile file,
            @RequestParam String sourceName) {
        // 1. 上传文件至MinIO
        String filePath = minioUtil.uploadFile(file);

        // 2. 创建数据源记录
        DataSource source = new DataSource();
        source.setSourceName(sourceName);
        source.setSourceType("Excel");
        source.setFilePath(filePath);
        boolean success = dataSourceService.save(source);

        return success ? Result.success("数据源创建成功") : Result.error("数据源创建失败");
    }

    /**
     * 新增数据源（数据库）
     */
    @PostMapping("/database")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "新增数据库数据源", description = "配置数据库连接信息")
    public Result<?> createDbSource(@Valid @RequestBody DataSourceReqDTO sourceDTO) {
        DataSource source = new DataSource();
        source.setSourceName(sourceDTO.getSourceName());
        source.setSourceType("数据库");
        source.setDbUrl(sourceDTO.getDbUrl());
        source.setDbUsername(sourceDTO.getDbUsername());
        source.setDbPassword(sourceDTO.getDbPassword());
        boolean success = dataSourceService.save(source);
        return success ? Result.success("数据源创建成功") : Result.error("数据源创建失败");
    }

    /**
     * 修改数据源
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "修改数据源配置",
            parameters = {@Parameter(name = "id", description = "数据源ID", required = true, example = "1")}
    )
    public Result<?> updateSource(@PathVariable Long id, @Valid @RequestBody DataSourceReqDTO sourceDTO) {
        DataSource source = dataSourceService.getById(id);
        if (source == null) {
            return Result.error("数据源不存在");
        }
        source.setSourceName(sourceDTO.getSourceName());
        source.setDbUrl(sourceDTO.getDbUrl());
        source.setDbUsername(sourceDTO.getDbUsername());
        source.setDbPassword(sourceDTO.getDbPassword());
        boolean success = dataSourceService.updateById(source);
        return success ? Result.success("数据源修改成功") : Result.error("数据源修改失败");
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(
            summary = "删除数据源",
            parameters = {@Parameter(name = "id", description = "数据源ID", required = true, example = "1")}
    )
    public Result<?> deleteSource(@PathVariable Long id) {
        DataSource source = dataSourceService.getById(id);
        if (source == null) {
            return Result.error("数据源不存在");
        }
        // 删除MinIO文件（如果是Excel类型）
        if ("Excel".equals(source.getSourceType()) && source.getFilePath() != null) {
            String fileName = source.getFilePath().substring(source.getFilePath().lastIndexOf("/") + 1);
            minioUtil.deleteFile(fileName);
        }
        boolean success = dataSourceService.removeById(id);
        return success ? Result.success("数据源删除成功") : Result.error("数据源删除失败");
    }

    /**
     * 测试数据库连接
     */
    @PostMapping("/test/connection")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "测试数据库连接", description = "验证数据库配置是否有效")
    public Result<?> testDbConnection(@RequestBody DbTestReqDTO sourceDTO) {
        boolean success = dataSourceService.testDbConnection(sourceDTO);
        return success ? Result.success("数据库连接成功") : Result.error("数据库连接失败");
    }
}