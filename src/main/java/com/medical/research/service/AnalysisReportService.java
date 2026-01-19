// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.analysis.AnalysisReport;

import java.util.List;
import java.util.Map;

public interface AnalysisReportService extends IService<AnalysisReport> {

    Map<String, Object> generateReport(AnalysisReport report) throws Exception;
    boolean deleteReportWithFile(Long id);
    Page<AnalysisReport> getReportPageList(List<Long> experimentIds, String reportName, Integer pageNum, Integer pageSize);
    void checkReport(Long reportId) throws Exception;
}

