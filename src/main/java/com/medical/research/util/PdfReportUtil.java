package com.medical.research.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medical.research.entity.analysis.AnalysisReport;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.research.ResearchData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class PdfReportUtil {
    @Value("${custom.pdf.save-path:/data1/pdf/}")
    private String pdfSavePath;

    public String generateReportPdf(AnalysisReport report, ExperimentPlan plan, List<ResearchData> dataList, String rocImagePath) throws Exception {
        File dir = new File(pdfSavePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String reportFileName = "科研分析报告_" + report.getReportName() + "_" + System.currentTimeMillis() + ".pdf";
        String pdfFullPath = pdfSavePath + reportFileName;
//        String pdfAccessPath = "/upload/report/" + reportFileName;

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(pdfFullPath)));
        document.open();

        BaseFont chineseBaseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(chineseBaseFont, 20, Font.BOLD);
        Font subTitleFont = new Font(chineseBaseFont, 14, Font.NORMAL);
        Font headingFont = new Font(chineseBaseFont, 14, Font.BOLD);
        Font contentFont = new Font(chineseBaseFont, 12, Font.NORMAL);
        Font tableHeaderFont = new Font(chineseBaseFont, 12, Font.BOLD);

        // 封面
        Paragraph title = new Paragraph(plan.getPlanName() + " - 分析报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph testMethod = new Paragraph("检验方法：" + report.getTestMethod(), subTitleFont);
        testMethod.setAlignment(Element.ALIGN_CENTER);
        document.add(testMethod);
        document.add(Chunk.NEWLINE);

        // 实验信息
        document.add(new Paragraph("一、实验基本信息", headingFont));
        document.add(Chunk.NEWLINE);
        PdfPTable planTable = new PdfPTable(2);
        planTable.setWidthPercentage(100);
        planTable.addCell(new PdfPCell(new Paragraph("实验名称", tableHeaderFont)));
        planTable.addCell(new Paragraph(plan.getPlanName(), contentFont));
        planTable.addCell(new PdfPCell(new Paragraph("研究目的", tableHeaderFont)));
        planTable.addCell(new Paragraph(plan.getResearchPurpose() == null ? "无" : plan.getResearchPurpose(), contentFont));
        planTable.addCell(new PdfPCell(new Paragraph("模型信息", tableHeaderFont)));
        planTable.addCell(new Paragraph(plan.getModelInfo() == null ? "无" : plan.getModelInfo(), contentFont));
        planTable.addCell(new PdfPCell(new Paragraph("创建时间", tableHeaderFont)));
        planTable.addCell(new Paragraph(plan.getCreateTime().toString(), contentFont));
        document.add(planTable);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // 统计结果
        document.add(new Paragraph("二、统计检验结果", headingFont));
        document.add(Chunk.NEWLINE);
        PdfPTable resultTable = new PdfPTable(2);
        resultTable.setWidthPercentage(100);
        resultTable.addCell(new PdfPCell(new Paragraph("统计指标", tableHeaderFont)));
        resultTable.addCell(new PdfPCell(new Paragraph("结果", tableHeaderFont)));
        resultTable.addCell(new Paragraph("模型1 AUC", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getAuc1()), contentFont));
        resultTable.addCell(new Paragraph("模型2 AUC", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getAuc2()), contentFont));
        resultTable.addCell(new Paragraph("AUC差异（模型1-模型2）", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getAucDiff()), contentFont));
        resultTable.addCell(new Paragraph("标准误", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getStdErr()), contentFont));
        resultTable.addCell(new Paragraph("Z值", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getZValue()), contentFont));
        resultTable.addCell(new Paragraph("双侧P值", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", report.getPValue()), contentFont));
        document.add(resultTable);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        //加roc图
        document.add(new Paragraph("三、ROC曲线图", headingFont));
        try {
            Image rocImage = Image.getInstance(rocImagePath);
            // 设置图像属性
            rocImage.setAlignment(Image.ALIGN_CENTER);
            rocImage.scaleToFit(500, 400); // 限制图像大小
            rocImage.setBorder(Image.BOX);
            rocImage.setBorderWidth(1);

            // 使用单列表格确保图像居中显示
            PdfPTable rocTable = new PdfPTable(1);
            rocTable.setWidthPercentage(100);
            PdfPCell imageCell = new PdfPCell(rocImage, true);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imageCell.setBorder(PdfPCell.NO_BORDER);
            rocTable.addCell(imageCell);
            document.add(rocTable);
        } catch (Exception e) {
            // 图像加载失败时的处理
            Paragraph errorMsg = new Paragraph("ROC图像加载失败: " + e.getMessage(), contentFont);
            errorMsg.setAlignment(Element.ALIGN_CENTER);
            document.add(errorMsg);
        }
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);



        // 结果解读
        document.add(new Paragraph("四、结果解读（医疗科研视角）", headingFont));
        document.add(Chunk.NEWLINE);
        Paragraph interpretation = new Paragraph(
                String.format("1. 模型性能：模型1 AUC=%.4f，模型2 AUC=%.4f，均处于较高水平，说明两个模型对该疾病的诊断效能优异；\n", report.getAuc1(), report.getAuc2()) +
                        String.format("2. 统计学差异：双侧P值=%.4f，%s；\n", report.getPValue(), report.getPValue().doubleValue() < 0.05 ? "提示两个模型AUC存在显著统计学差异" : "提示两个模型AUC无显著统计学差异") +
                        "3. 科研建议：建议扩大样本量进一步验证，或结合临床特征优化模型结构，提升诊断特异性。",
                contentFont
        );
        document.add(interpretation);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // 原始数据
        document.add(new Paragraph("五、附录：原始科研数据", headingFont));
        document.add(Chunk.NEWLINE);
        PdfPTable dataTable = new PdfPTable(3);
        dataTable.setWidthPercentage(100);
        dataTable.addCell(new PdfPCell(new Paragraph("真实标签", tableHeaderFont)));
        dataTable.addCell(new PdfPCell(new Paragraph("模型1评分", tableHeaderFont)));
        dataTable.addCell(new PdfPCell(new Paragraph("模型2评分", tableHeaderFont)));
        for (ResearchData data : dataList) {
            dataTable.addCell(new Paragraph(data.getTrueLabel().toString(), contentFont));
            dataTable.addCell(new Paragraph(String.format("%.2f", data.getModel1Score()), contentFont));
            dataTable.addCell(new Paragraph(String.format("%.2f", data.getModel2Score()), contentFont));
        }
        document.add(dataTable);

        document.close();
        log.info("PDF报告生成成功：{}", pdfFullPath);
        return pdfFullPath;
    }
}