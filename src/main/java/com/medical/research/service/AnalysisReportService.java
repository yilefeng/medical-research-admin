// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.analysis.AnalysisReport;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface AnalysisReportService extends IService<AnalysisReport> {

    Map<String, Object> generateReport(AnalysisReport report) throws Exception;
    Map<String, Object> getReportDetail(Long reportId);
    Map<String, Object> getRocData(Long reportId);
    void previewPdf(Long id, HttpServletResponse response) throws Exception;
    void downloadPdf(Long id, HttpServletResponse response) throws Exception;
    void previewRocImage(Long id, HttpServletResponse response) throws Exception;
    boolean deleteReportWithFile(Long id);

    Object getReportPageList(List<Long> experimentIds, String reportName, Integer pageNum, Integer pageSize);

    void checkReport(Long reportId) throws Exception;
}

