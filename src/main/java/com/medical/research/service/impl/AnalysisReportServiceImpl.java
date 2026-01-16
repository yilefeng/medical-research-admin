package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.research.ResearchData;
import com.medical.research.exception.BusinessException;
import com.medical.research.mapper.AnalysisReportMapper;
import com.medical.research.mapper.ExperimentPlanMapper;
import com.medical.research.mapper.ResearchDataMapper;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.util.PdfReportUtil;
import com.medical.research.util.RocChartUtil;
import com.medical.research.util.StatTestUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport> implements AnalysisReportService {

    @Autowired
    private ResearchDataMapper researchDataMapper;
    @Autowired
    private ExperimentPlanMapper experimentPlanMapper;
    @Autowired
    private PdfReportUtil pdfReportUtil;
    @Autowired
    private RocChartUtil rocChartUtil;

    @Value("${custom.pdf.save-path:/data1/pdf}")
    private String pdfSavePath;

    @Autowired
    private ExperimentPlanService experimentPlanService;

    @Override
    public Map<String, Object> generateReport(AnalysisReport report) throws Exception {

        List<ResearchData> dataList = researchDataMapper.selectList(new LambdaQueryWrapper<ResearchData>()
                .eq(ResearchData::getExperimentId, report.getExperimentId()));
        ExperimentPlan plan = experimentPlanMapper.selectById(report.getExperimentId());

        // 2. 提取标签和评分
        List<Integer> labels = dataList.stream().map(ResearchData::getTrueLabel).collect(Collectors.toList());
        List<Double> scores1 = dataList.stream().map(ResearchData::getModel1Score).collect(Collectors.toList());
        List<Double> scores2 = dataList.stream().map(ResearchData::getModel2Score).collect(Collectors.toList());

        // 3. 统计计算+ROC数据
        Map<String, Object> statResult = StatTestUtil.delongTestWithRoc(labels, scores1, scores2);
        double auc1 = (double) statResult.get("auc1");
        double auc2 = (double) statResult.get("auc2");

        // 4. 生成ROC图片
        List<Double> fpr1 = Arrays.stream((double[]) statResult.get("fpr1")).boxed().collect(Collectors.toList());
        List<Double> tpr1 = Arrays.stream((double[]) statResult.get("tpr1")).boxed().collect(Collectors.toList());
        List<Double> fpr2 = Arrays.stream((double[]) statResult.get("fpr2")).boxed().collect(Collectors.toList());
        List<Double> tpr2 = Arrays.stream((double[]) statResult.get("tpr2")).boxed().collect(Collectors.toList());
        String rocImagePath = rocChartUtil.generateRocChart(fpr1, tpr1, auc1, fpr2, tpr2, auc2, report.getReportName());

        report.setAuc1(BigDecimal.valueOf(auc1));
        report.setAuc2(BigDecimal.valueOf(auc2));
        report.setAucDiff(BigDecimal.valueOf((double) statResult.get("aucDiff")));
        report.setStdErr(BigDecimal.valueOf((double) statResult.get("stdErr")));
        report.setZValue(BigDecimal.valueOf((double) statResult.get("zValue")));
        report.setPValue(BigDecimal.valueOf((double) statResult.get("pValue")));

        // 5. 生成PDF
        String pdfPath = pdfReportUtil.generateReportPdf(report, plan, dataList, rocImagePath);

        // 6. 保存报告
        report.setPdfPath(pdfPath);
        report.setCreateTime(LocalDateTime.now());
        this.save(report);

        // 7. 返回结果
        statResult.put("reportId", report.getId());
        statResult.put("pdfPath", pdfPath);
        statResult.put("rocImagePath", rocImagePath);
        return statResult;
    }

    @Override
    public Map<String, Object> getReportDetail(Long reportId) {
        AnalysisReport report = this.getById(reportId);
        Map<String, Object> detail = new HashMap<>();
        if (report != null) {
            detail.put("report", report);
            // 补充ROC数据
            detail.putAll(getRocData(reportId));
        }
        return detail;
    }

    @Override
    public Map<String, Object> getRocData(Long reportId) {
        AnalysisReport report = this.getById(reportId);
        Map<String, Object> rocData = new HashMap<>();
        if (report != null) {
            List<ResearchData> dataList = researchDataMapper.selectList(new LambdaQueryWrapper<ResearchData>()
                    .eq(ResearchData::getExperimentId, report.getExperimentId()));
            List<Integer> labels = dataList.stream().map(ResearchData::getTrueLabel).collect(Collectors.toList());
            List<Double> scores1 = dataList.stream().map(ResearchData::getModel1Score).collect(Collectors.toList());
            List<Double> scores2 = dataList.stream().map(ResearchData::getModel2Score).collect(Collectors.toList());

            double[][] roc1 = RocChartUtil.calculateRocData(labels, scores1);
            double[][] roc2 = RocChartUtil.calculateRocData(labels, scores2);
            rocData.put("fpr1", roc1[0]);
            rocData.put("tpr1", roc1[1]);
            rocData.put("fpr2", roc2[0]);
            rocData.put("tpr2", roc2[1]);
        }
        return rocData;
    }

    @Override
    public void previewPdf(Long id, HttpServletResponse response) throws Exception {
        AnalysisReport report = this.getById(id);
        if (report == null || report.getPdfPath() == null) {
            throw new Exception("PDF报告不存在");
        }
        String pdfLocalPath = report.getPdfPath().replace("/upload/report/", pdfSavePath);
        File pdfFile = new File(pdfLocalPath);
        if (!pdfFile.exists()) {
            throw new Exception("PDF文件不存在");
        }

        // 返回文件流
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=" + pdfFile.getName());
        try (FileInputStream fis = new FileInputStream(pdfFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }

    @Override
    public void downloadPdf(Long id, HttpServletResponse response) throws Exception {
        AnalysisReport report = this.getById(id);
        if (report == null || report.getPdfPath() == null) {
            throw new Exception("PDF报告不存在");
        }
        String pdfLocalPath = report.getPdfPath().replace("/upload/report/", pdfSavePath);
        File pdfFile = new File(pdfLocalPath);
        if (!pdfFile.exists()) {
            throw new Exception("PDF文件不存在");
        }

        // 触发下载
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + pdfFile.getName());
        try (FileInputStream fis = new FileInputStream(pdfFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }

    @Override
    public void previewRocImage(Long id, HttpServletResponse response) throws Exception {
        AnalysisReport report = this.getById(id);
        if (report == null || report.getRocImagePath() == null) {
            throw new Exception("ROC图片不存在");
        }
        String rocLocalPath = report.getRocImagePath().replace("/upload/report/roc/", pdfSavePath + "roc/");
        File rocFile = new File(rocLocalPath);
        if (!rocFile.exists()) {
            throw new Exception("ROC图片文件不存在");
        }

        // 返回图片流
        response.setContentType("image/png");
        try (FileInputStream fis = new FileInputStream(rocFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        }
    }

    @Override
    public boolean deleteReportWithFile(Long id) {
        AnalysisReport report = this.getById(id);
        if (report == null) {
            return false;
        }
        // 删除PDF文件
        if (report.getPdfPath() != null) {
            String pdfLocalPath = report.getPdfPath().replace("/upload/report/", pdfSavePath);
            File pdfFile = new File(pdfLocalPath);
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
        }
        // 删除ROC图片
        if (report.getRocImagePath() != null) {
            String rocLocalPath = report.getRocImagePath().replace("/upload/report/roc/", pdfSavePath + "roc/");
            File rocFile = new File(rocLocalPath);
            if (rocFile.exists()) {
                rocFile.delete();
            }
        }
        // 删除数据库记录
        return this.removeById(id);
    }

    // 报告分页查询（供报告管理模块调用）
    public Object getReportPageList(List<Long> experimentIds, String reportName, Integer pageNum, Integer pageSize) {
        if (experimentIds == null || experimentIds.isEmpty()) {
            return new Page<>();
        }
        Page<AnalysisReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AnalysisReport> wrapper = new LambdaQueryWrapper<>();
        if (reportName != null && !reportName.isEmpty()) {
            wrapper.like(AnalysisReport::getReportName, reportName);
        }
        if (CollectionUtils.isNotEmpty(experimentIds)) {
            wrapper.in(AnalysisReport::getExperimentId, experimentIds);
        }
        wrapper.orderByDesc(AnalysisReport::getId);
        return this.page(page, wrapper);
    }

    @Override
    public void checkReport(Long reportId) {
        AnalysisReport report = getById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        experimentPlanService.checkRightExperimentPlan(report.getExperimentId());
    }
}