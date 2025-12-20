// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.AnalysisReport;
import com.medical.research.dto.report.AnalysisReportReqDTO;
import com.medical.research.dto.report.AnalysisReportRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface AnalysisReportService extends IService<AnalysisReport> {

    /**
     * 分页查询分析报告
     */
    Page<AnalysisReportRespDTO> getReportPage(AnalysisReportReqDTO req);

    /**
     * 生成分析报告
     */
    boolean generateReport(AnalysisReportReqDTO req);

    /**
     * 根据ID查询报告详情
     */
    AnalysisReportRespDTO getReportById(Long id);

    /**
     * 导出PDF报告
     */
    String exportPdf(Long id);

    /**
     * 作废报告
     */
    boolean invalidReport(Long id);
}

