package com.medical.research.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.medical.research.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * PDF导出工具类：生成统计报告PDF
 */
@Slf4j
@Component
public class PdfExportUtil {

    /**
     * 生成统计报告PDF
     */
    public byte[] generateReportPdf(String reportName, Map<String, Object> statResult, String content) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 创建PDF文档
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font contentFont = new Font(bfChinese, 12, Font.NORMAL);
            Font tableFont = new Font(bfChinese, 10, Font.NORMAL);

            // 添加标题
            Paragraph title = new Paragraph(reportName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // 添加统计结果表格
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // 表格标题行
            PdfPCell cell1 = new PdfPCell(new Paragraph("统计指标", tableFont));
            cell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Paragraph("结果", tableFont));
            cell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell2);

            // 表格数据行
            for (Map.Entry<String, Object> entry : statResult.entrySet()) {
                table.addCell(new PdfPCell(new Paragraph(entry.getKey(), tableFont)));
                table.addCell(new PdfPCell(new Paragraph(entry.getValue().toString(), tableFont)));
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            // 添加报告内容
            Paragraph contentTitle = new Paragraph("详细分析：", contentFont);
            contentTitle.setSpacingBefore(5f);
            document.add(contentTitle);
            document.add(new Paragraph(content, contentFont));

            // 关闭文档
            document.close();
            log.info("PDF报告生成成功，大小：{}字节", baos.size());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("PDF生成失败", e);
            throw new BusinessException("PDF报告生成失败：" + e.getMessage());
        }
    }
}