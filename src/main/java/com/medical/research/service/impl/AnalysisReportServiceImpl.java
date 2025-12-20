package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.AnalysisReport;
import com.medical.research.entity.ExperimentPlan;
import com.medical.research.entity.ResearchData;
import com.medical.research.entity.StatModel;
import com.medical.research.mapper.AnalysisReportMapper;
import com.medical.research.dto.report.AnalysisReportReqDTO;
import com.medical.research.dto.report.AnalysisReportRespDTO;
import com.medical.research.service.AnalysisReportService;
import com.medical.research.service.ExperimentPlanService;
import com.medical.research.service.ResearchDataService;
import com.medical.research.service.StatModelService;
import com.medical.research.util.SecurityUserUtil;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalysisReportServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport>
        implements AnalysisReportService {

    @Resource
    private AnalysisReportMapper analysisReportMapper;

    @Resource
    private ExperimentPlanService experimentPlanService;

    @Resource
    private StatModelService statModelService;

    // 核心：注入科研数据服务，查询research_data表
    @Resource
    private ResearchDataService researchDataService;

    // 统计工具类实例
    private final TTest tTest = new TTest();
    private final ChiSquareTest chiSquareTest = new ChiSquareTest();
    private final SimpleRegression simpleRegression = new SimpleRegression();
    private final OneWayAnova oneWayAnova = new OneWayAnova();

    @Override
    public Page<AnalysisReportRespDTO> getReportPage(AnalysisReportReqDTO req) {
        Page<AnalysisReportRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = analysisReportMapper.selectReportCount(req);
        page.setTotal(total);

        // 查询列表
        List<AnalysisReport> reportList = analysisReportMapper.selectReportPage(req);
        List<AnalysisReportRespDTO> respList = reportList.stream().map(report -> {
            AnalysisReportRespDTO resp = new AnalysisReportRespDTO();
            BeanUtils.copyProperties(report, resp);

            // 补充实验方案名称
            ExperimentPlan plan = experimentPlanService.getById(report.getPlanId());
            if (plan != null) {
                resp.setPlanName(plan.getPlanName());
            }

            // 补充模型名称
            StatModel model = statModelService.getById(report.getModelId());
            if (model != null) {
                resp.setModelName(model.getModelName());
            }
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateReport(AnalysisReportReqDTO req) {
        // 1. 校验参数
        if (req.getPlanId() == null || req.getModelId() == null) {
            return false;
        }

        // 2. 获取统计模型
        StatModel statModel = statModelService.getById(req.getModelId());
        if (statModel == null) {
            return false;
        }

        // 3. 获取实验方案，关联experimentNo（用于查询research_data表）
        ExperimentPlan plan = experimentPlanService.getById(req.getPlanId());
        if (plan == null || !org.springframework.util.StringUtils.hasText(plan.getExperimentNo())) {
            return false;
        }

        // 4. 核心：从research_data表查询该实验的所有数据
        List<ResearchDataRespDTO> researchDataList = researchDataService.getDataByExperimentNo(plan.getExperimentNo());
        if (CollectionUtils.isEmpty(researchDataList)) {
            throw new RuntimeException("该实验暂无科研数据，无法生成分析报告");
        }

        // 5. 调用统计模型生成报告内容（基于真实数据）
        String reportContent = statModelService.executeModel(
                statModel.getModelCode(),
                req.getStatConditions(),
                researchDataList // 传入真实数据列表
        );

        // 6. 保存报告
        AnalysisReport report = new AnalysisReport();
        BeanUtils.copyProperties(req, report);
        report.setReportContent(reportContent);
        report.setStatus(1); // 已生成
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setUserId(SecurityUserUtil.getCurrentUserId());
        return save(report);
    }

    @Override
    public AnalysisReportRespDTO getReportById(Long id) {
        AnalysisReport report = getById(id);
        if (report == null) {
            return null;
        }

        AnalysisReportRespDTO resp = new AnalysisReportRespDTO();
        BeanUtils.copyProperties(report, resp);

        // 补充关联信息
        ExperimentPlan plan = experimentPlanService.getById(report.getPlanId());
        if (plan != null) {
            resp.setPlanName(plan.getPlanName());
        }

        StatModel model = statModelService.getById(report.getModelId());
        if (model != null) {
            resp.setModelName(model.getModelName());
        }

        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String exportPdf(Long id) {
        AnalysisReport report = getById(id);
        if (report == null || report.getStatus() == 3) { // 已作废
            return null;
        }

        try {
            // 生成PDF文件路径
            String pdfPath = System.getProperty("user.dir") + "/pdf/report_" + id + "_" + System.currentTimeMillis() + ".pdf";

            // 确保目录存在
            File pdfDir = new File(System.getProperty("user.dir") + "/pdf");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }

            // 将HTML报告内容转换为PDF
            generatePdfFromHtml(report.getReportContent(), pdfPath);

            // 更新报告状态为已导出
            report.setStatus(2);
            report.setUpdateTime(LocalDateTime.now());
            updateById(report);

            return pdfPath;
        } catch (Exception e) {
            log.error("PDF生成失败", e);
            throw new RuntimeException("PDF生成失败：" + e.getMessage());
        }
    }

    public String generatePdfFromHtmlNew(String htmlContent, String pdfPath) {
        try {
            // 包装HTML内容为完整且格式正确的XHTML文档
            String fullHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
                    "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                    "<title>分析报告</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    htmlContent + "\n" +
                    "</body>\n" +
                    "</html>";

            OutputStream os = Files.newOutputStream(Paths.get(pdfPath));
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(fullHtml);
            renderer.layout();
            renderer.createPDF(os);
            os.close();

            return pdfPath;
        } catch (Exception e) {
            log.error("PDF生成失败，HTML内容: {}", new Exception(htmlContent));
            throw new RuntimeException("PDF生成失败：" + e.getMessage());
        }
    }

    public String generatePdfFromHtml(String htmlContent, String pdfPath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(pdfPath)));
            document.open();

            // 支持中文的字体设置
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font font = new Font(baseFont, 12);

            // 手动解析简单的HTML标签
            parseSimpleHtml(document, htmlContent, font);

            document.close();
            return pdfPath;
        } catch (Exception e) {
            log.error("PDF生成失败，HTML内容: {}", new Exception(htmlContent));
            throw new RuntimeException("PDF生成失败：" + e.getMessage());
        }
    }

    private void parseSimpleHtml(Document document, String htmlContent, Font font) throws Exception {
        // 简单的HTML解析处理
        String[] parts = htmlContent.split("(?=<h[1-6]>|</h[1-6]>|<p>|</p>)");

        for (String part : parts) {
            if (part.startsWith("<h3>")) {
                String content = part.replaceAll("<h3>|</h3>", "").trim();
                Font headerFont = new Font(font.getBaseFont(), 16, Font.BOLD);
                document.add(new Paragraph(content, headerFont));
            } else if (part.startsWith("<p>")) {
                String content = part.replaceAll("<p>|</p>", "").trim();
                document.add(new Paragraph(content, font));
            } else if (!part.trim().isEmpty() && !part.startsWith("</")) {
                // 处理剩余文本
                document.add(new Paragraph(part.trim(), font));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invalidReport(Long id) {
        AnalysisReport report = getById(id);
        if (report == null) {
            return false;
        }

        report.setStatus(3); // 已作废
        report.setUpdateTime(LocalDateTime.now());
        return updateById(report);
    }
}