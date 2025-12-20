package com.medical.research.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medical.research.dto.research.ResearchDataReqDTO;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.ResearchData;
import com.medical.research.service.ResearchDataService;
import com.medical.research.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 科研数据控制器
 */
@RestController
@RequestMapping("/research/data")
@RequiredArgsConstructor
@Tag(name = "科研数据模块", description = "科研数据的增删改查、Excel导入导出、可视化数据接口")
public class ResearchDataController {
    private final ResearchDataService researchDataService;

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String CHARSET_ENCODING = "utf-8";
    private static final String FILE_EXTENSION = ".xlsx";

    /**
     * 分页查询科研数据
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "分页查询科研数据",
            description = "支持按实验编号、模型名称、数据集筛选",
            parameters = {
                    @Parameter(name = "pageNum", description = "页码", required = true, example = "1"),
                    @Parameter(name = "pageSize", description = "页大小", required = true, example = "10"),
                    @Parameter(name = "experimentNo", description = "实验编号", example = "EXP2025001"),
                    @Parameter(name = "modelName", description = "模型名称", example = "ResNet50"),
                    @Parameter(name = "dataset", description = "数据集名称", example = "LungCT-2024")
            }
    )
    public Result<IPage<ResearchDataRespDTO>> getDataPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String experimentNo,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String dataset) {

        ResearchDataReqDTO page = new ResearchDataReqDTO();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setExperimentNo(experimentNo);
        page.setModelName(modelName);
        page.setDataset(dataset);

        IPage<ResearchDataRespDTO> result = researchDataService.getDataPage(page);
        return Result.success("查询成功", result);
    }

    /**
     * 根据ID查询科研数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "查询科研数据详情",
            parameters = {@Parameter(name = "id", description = "科研数据ID", required = true, example = "1")}
    )
    public Result<ResearchData> getDataById(@PathVariable Long id) {
        ResearchData data = researchDataService.getById(id);
        if (data == null) {
            return Result.error("科研数据不存在");
        }
        return Result.success("查询成功", data);
    }

    /**
     * 新增科研数据
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(summary = "新增科研数据", description = "添加单条模型性能数据")
    public Result<?> createData(@RequestBody ResearchDataReqDTO dataDTO) {
        boolean success = researchDataService.addData(dataDTO);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 修改科研数据
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "修改科研数据",
            parameters = {@Parameter(name = "id", description = "科研数据ID", required = true, example = "1")}
    )
    public Result<?> updateData(@PathVariable Long id, @RequestBody ResearchDataReqDTO dataDTO) {
        boolean success = researchDataService.updateData(dataDTO);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除科研数据
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "删除科研数据",
            parameters = {@Parameter(name = "id", description = "科研数据ID", required = true, example = "1")}
    )
    public Result<?> deleteData(@PathVariable Long id) {
        boolean success = researchDataService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * Excel导入科研数据
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "Excel批量导入科研数据",
            parameters = {@Parameter(name = "file", description = "Excel文件（.xlsx/.xls）", required = true)}
    )
    public Result<?> importResearchData(@RequestParam("file") MultipartFile file) {
        try {
            boolean imported = researchDataService.importDataByEasyExcel(file);
            if (!imported) {
                return Result.error("数据导入失败");
            }
            return Result.success("Excel数据导入成功");
        } catch (Exception e) {
            return Result.error("数据导入失败：" + e.getMessage());
        }
    }

    /**
     * Excel导出科研数据
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(
            summary = "导出科研数据为Excel",
            description = "支持按实验编号筛选导出"
    )
    public void exportResearchData(
            @RequestParam(required = false) String experimentNo,
            HttpServletResponse response) throws IOException {
        try {
            response.setContentType(EXCEL_CONTENT_TYPE);
            response.setCharacterEncoding(CHARSET_ENCODING);
            String fileName = URLEncoder.encode("科研数据_" + System.currentTimeMillis(), "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName + FILE_EXTENSION);

            // 查询数据并导出
            List<ResearchDataRespDTO> dataList = researchDataService.getDataByExperimentNo(experimentNo);
            if (dataList == null) {
                dataList = new ArrayList<>();
            }

            EasyExcel.write(response.getOutputStream(), ResearchDataRespDTO.class)
                    .sheet("科研数据")
                    .doWrite(dataList);
        } catch (UnsupportedEncodingException e) {
            // 处理编码异常
            throw new RuntimeException("文件名编码失败", e);
        } catch (IOException e) {
            // 处理IO异常
            throw new RuntimeException("导出文件流处理失败", e);
        } catch (Exception e) {
            // 处理其他业务异常
            throw new RuntimeException("数据导出失败", e);
        }
    }

    /**
     * 获取可视化数据
     */
    @GetMapping("/visual")
    @PreAuthorize("hasAnyRole('admin','researcher')")
    @Operation(summary = "获取可视化数据", description = "返回用于ECharts渲染的模型性能数据")
    public Result<List<ResearchDataRespDTO>> getVisualData(@RequestParam String experimentNo) {
        List<ResearchDataRespDTO> dataList = researchDataService.getDataByExperimentNo(experimentNo);
        return Result.success("查询成功", dataList);
    }
}