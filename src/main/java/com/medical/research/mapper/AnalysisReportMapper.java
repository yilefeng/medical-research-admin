package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.dto.report.AnalysisReportReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnalysisReportMapper extends BaseMapper<AnalysisReport> {

    /**
     * 分页查询分析报告
     */
    List<AnalysisReport> selectReportPage(@Param("req") AnalysisReportReqDTO req);

    /**
     * 查询分析报告总数
     */
    Long selectReportCount(@Param("req") AnalysisReportReqDTO req);
}