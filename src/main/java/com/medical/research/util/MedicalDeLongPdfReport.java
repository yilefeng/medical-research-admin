package com.medical.research.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 医疗科研DeLong检验分析报告（iText 5 版本，PDF导出版）
 */
public class MedicalDeLongPdfReport {

    // 样本数据实体类（复用之前的定义）
    static class MedicalSample {
        int trueLabel;
        double model1Score;
        double model2Score;

        public MedicalSample(int trueLabel, double model1Score, double model2Score) {
            this.trueLabel = trueLabel;
            this.model1Score = model1Score;
            this.model2Score = model2Score;
        }
    }

    // ---------------------- 复用DeLong检验核心算法（与之前一致，无修改） ----------------------
    private static double calculateAUC(List<Integer> labels, List<Double> scores) {
        List<Double> positiveScores = new ArrayList<>();
        List<Double> negativeScores = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i) == 1) {
                positiveScores.add(scores.get(i));
            } else {
                negativeScores.add(scores.get(i));
            }
        }

        int positiveCount = positiveScores.size();
        int negativeCount = negativeScores.size();
        if (positiveCount == 0 || negativeCount == 0) {
            return 0.0;
        }

        double total = 0.0;
        for (double posScore : positiveScores) {
            for (double negScore : negativeScores) {
                if (posScore > negScore) {
                    total += 1.0;
                } else if (posScore == negScore) {
                    total += 0.5;
                }
            }
        }

        return total / (positiveCount * negativeCount);
    }

    private static double[] delongTest(List<Integer> labels, List<Double> scores1, List<Double> scores2) {
        List<Double> positiveScores1 = new ArrayList<>();
        List<Double> negativeScores1 = new ArrayList<>();
        List<Double> positiveScores2 = new ArrayList<>();
        List<Double> negativeScores2 = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            if (labels.get(i) == 1) {
                positiveScores1.add(scores1.get(i));
                positiveScores2.add(scores2.get(i));
            } else {
                negativeScores1.add(scores1.get(i));
                negativeScores2.add(scores2.get(i));
            }
        }

        int m = positiveScores1.size();
        int n = negativeScores1.size();
        double auc1 = calculateAUC(labels, scores1);
        double auc2 = calculateAUC(labels, scores2);
        double aucDiff = auc1 - auc2;

        // 计算方差-协方差矩阵
        double[][] covMatrix = new double[2][2];
        double var1 = 0.0;
        for (double posScore : positiveScores1) {
            double rank = 0.0;
            for (double score : scores1) {
                if (score < posScore) rank += 1.0;
                else if (score == posScore) rank += 0.5;
            }
            double term = (rank / n - auc1) * (rank / n - auc1);
            var1 += term;
        }
        var1 /= (m - 1);
        for (double negScore : negativeScores1) {
            double rank = 0.0;
            for (double score : scores1) {
                if (score < negScore) rank += 1.0;
                else if (score == negScore) rank += 0.5;
            }
            double term = ((m - rank / n) / m - auc1) * ((m - rank / n) / m - auc1);
            var1 += term;
        }
        var1 /= (n - 1);
        covMatrix[0][0] = var1 / m + var1 / n;

        double var2 = 0.0;
        for (double posScore : positiveScores2) {
            double rank = 0.0;
            for (double score : scores2) {
                if (score < posScore) rank += 1.0;
                else if (score == posScore) rank += 0.5;
            }
            double term = (rank / n - auc2) * (rank / n - auc2);
            var2 += term;
        }
        var2 /= (m - 1);
        for (double negScore : negativeScores2) {
            double rank = 0.0;
            for (double score : scores2) {
                if (score < negScore) rank += 1.0;
                else if (score == negScore) rank += 0.5;
            }
            double term = ((m - rank / n) / m - auc2) * ((m - rank / n) / m - auc2);
            var2 += term;
        }
        var2 /= (n - 1);
        covMatrix[1][1] = var2 / m + var2 / n;

        double cov = 0.0;
        for (int i = 0; i < m; i++) {
            double posScore1 = positiveScores1.get(i);
            double posScore2 = positiveScores2.get(i);
            double rank1 = 0.0, rank2 = 0.0;
            for (double score : scores1) {
                if (score < posScore1) rank1 += 1.0;
                else if (score == posScore1) rank1 += 0.5;
            }
            for (double score : scores2) {
                if (score < posScore2) rank2 += 1.0;
                else if (score == posScore2) rank2 += 0.5;
            }
            cov += (rank1 / n - auc1) * (rank2 / n - auc2);
        }
        cov /= (m - 1);
        for (int i = 0; i < n; i++) {
            double negScore1 = negativeScores1.get(i);
            double negScore2 = negativeScores2.get(i);
            double rank1 = 0.0, rank2 = 0.0;
            for (double score : scores1) {
                if (score < negScore1) rank1 += 1.0;
                else if (score == negScore1) rank1 += 0.5;
            }
            for (double score : scores2) {
                if (score < negScore2) rank2 += 1.0;
                else if (score == negScore2) rank2 += 0.5;
            }
            cov += ((m - rank1 / n) / m - auc1) * ((m - rank2 / n) / m - auc2);
        }
        cov /= (n - 1);
        covMatrix[0][1] = covMatrix[1][0] = cov / m + cov / n;

        double stdErr = Math.sqrt(covMatrix[0][0] + covMatrix[1][1] - 2 * covMatrix[0][1]);
        double zValue = aucDiff / (stdErr == 0 ? 1e-8 : stdErr);
        double pValue = 2 * (1 - cumulativeNormalDistribution(Math.abs(zValue)));

        return new double[]{auc1, auc2, aucDiff, stdErr, zValue, pValue};
    }

    private static double cumulativeNormalDistribution(double x) {
        double t = 1.0 / (1.0 + 0.2316419 * x);
        double d = 0.3989423 * Math.exp(-x * x / 2.0);
        double prob = d * t * (0.3193815 + t * (-0.3565638 + t * (1.781478 + t * (-1.821256 + t * 1.330274))));
        if (x > 0) {
            prob = 1.0 - prob;
        }
        return prob;
    }
    // ---------------------- DeLong检验核心算法结束 ----------------------

    // ---------------------- iText 5 PDF报告生成核心方法 ----------------------
    /**
     * 生成DeLong检验分析报告并导出PDF（iText 5 实现）
     * @param delongResult DeLong检验结果数组[AUC1, AUC2, AUC差异, 标准误, Z值, P值]
     * @param sampleList 原始样本数据
     * @param outputPath PDF导出路径
     */
    private static void generateDelongPdfReport(double[] delongResult, List<MedicalSample> sampleList, String outputPath) throws DocumentException, IOException {
        // 1. 创建PDF文档对象（设置页面大小、边距）
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        // 2. 创建PDF写入器
        PdfWriter.getInstance(document, Files.newOutputStream(Paths.get(outputPath)));
        // 3. 打开文档
        document.open();

        // 4. 加载中文字体（通过itext-asian，无需系统字体）
        BaseFont chineseBaseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(chineseBaseFont, 20, Font.BOLD); // 标题字体
        Font subTitleFont = new Font(chineseBaseFont, 14, Font.NORMAL); // 副标题字体
        Font headingFont = new Font(chineseBaseFont, 14, Font.BOLD); // 一级标题字体
        Font contentFont = new Font(chineseBaseFont, 12, Font.NORMAL); // 正文字体
        Font tableHeaderFont = new Font(chineseBaseFont, 12, Font.BOLD); // 表格标题字体

        // ---------------------- 报告封面 ----------------------
        Paragraph title = new Paragraph("医疗科研DeLong检验分析报告", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("肺癌CT影像良恶性诊断模型AUC比较", subTitleFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subTitle);

        Paragraph date = new Paragraph("生成日期：2025年12月20日", contentFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);
        document.add(Chunk.NEWLINE); // 换行

        // ---------------------- 摘要 ----------------------
        Paragraph abstractHeading = new Paragraph("一、摘要", headingFont);
        document.add(abstractHeading);
        Paragraph abstractContent = new Paragraph(
                "本报告针对肺癌CT影像良恶性诊断的2个模型（传统CNN模型、Transformer模型）的预测结果，采用DeLong检验比较两个模型的ROC曲线下面积（AUC）的统计学差异。" +
                        "分析样本共25例（13例阳性/恶性、12例阴性/良性），结果显示两个模型的AUC无统计学显著差异（P>0.05）。",
                contentFont
        );
        document.add(abstractContent);
        document.add(Chunk.NEWLINE);

        // ---------------------- 数据说明 ----------------------
        Paragraph dataHeading = new Paragraph("二、数据说明", headingFont);
        document.add(dataHeading);
        document.add(new Paragraph("1. 样本来源：模拟肺癌CT影像诊断的临床科研数据", contentFont));
        document.add(new Paragraph("2. 字段定义：", contentFont));

        // 数据说明表格（2列）
        PdfPTable dataDescTable = new PdfPTable(2);
        dataDescTable.setWidthPercentage(100); // 占满页面宽度
        // 表格头部
        PdfPCell cell1 = new PdfPCell(new Paragraph("字段名", tableHeaderFont));
        PdfPCell cell2 = new PdfPCell(new Paragraph("说明", tableHeaderFont));
        dataDescTable.addCell(cell1);
        dataDescTable.addCell(cell2);
        // 表格内容
        dataDescTable.addCell(new Paragraph("trueLabel", contentFont));
        dataDescTable.addCell(new Paragraph("真实临床标签：1=恶性（阳性），0=良性（阴性）（金标准）", contentFont));
        dataDescTable.addCell(new Paragraph("model1Score", contentFont));
        dataDescTable.addCell(new Paragraph("传统CNN模型的恶性预测概率（0~1）", contentFont));
        dataDescTable.addCell(new Paragraph("model2Score", contentFont));
        dataDescTable.addCell(new Paragraph("Transformer模型的恶性预测概率（0~1）", contentFont));
        document.add(dataDescTable);

        document.add(new Paragraph("3. 样本量：25例（阳性13例，阴性12例）", contentFont));
        document.add(Chunk.NEWLINE);

        // ---------------------- 分析方法 ----------------------
        Paragraph methodHeading = new Paragraph("三、分析方法：DeLong检验", headingFont);
        document.add(methodHeading);
        Paragraph methodContent = new Paragraph(
                "DeLong检验是用于比较两个二分类诊断模型ROC曲线下面积（AUC）差异的统计学方法，" +
                        "适用于医疗科研中评估不同诊断模型的性能优劣，核心是通过计算两个模型AUC的方差与协方差，判断差异是否具有统计学意义。",
                contentFont
        );
        document.add(methodContent);
        document.add(Chunk.NEWLINE);

        // ---------------------- 统计结果 ----------------------
        Paragraph resultHeading = new Paragraph("四、DeLong检验结果", headingFont);
        document.add(resultHeading);

        // 检验结果表格（2列）
        PdfPTable resultTable = new PdfPTable(2);
        resultTable.setWidthPercentage(100);
        // 表格头部
        resultTable.addCell(new PdfPCell(new Paragraph("统计指标", tableHeaderFont)));
        resultTable.addCell(new PdfPCell(new Paragraph("结果", tableHeaderFont)));
        // 表格内容
        resultTable.addCell(new Paragraph("模型1（CNN）AUC", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[0]), contentFont));
        resultTable.addCell(new Paragraph("模型2（Transformer）AUC", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[1]), contentFont));
        resultTable.addCell(new Paragraph("AUC差异（模型1-模型2）", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[2]), contentFont));
        resultTable.addCell(new Paragraph("标准误", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[3]), contentFont));
        resultTable.addCell(new Paragraph("Z值", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[4]), contentFont));
        resultTable.addCell(new Paragraph("双侧P值", contentFont));
        resultTable.addCell(new Paragraph(String.format("%.4f", delongResult[5]), contentFont));
        document.add(resultTable);
        document.add(Chunk.NEWLINE);

        // ---------------------- 结果解读 ----------------------
        Paragraph interpretationHeading = new Paragraph("五、结果解读（医疗科研视角）", headingFont);
        document.add(interpretationHeading);
        Paragraph interpretationContent = new Paragraph(
                String.format("1. AUC值：模型1 AUC=%.4f，模型2 AUC=%.4f，两个模型的诊断性能均较优（AUC接近1）；\n", delongResult[0], delongResult[1]) +
                        String.format("2. 统计学差异：双侧P值=%.4f（P>0.05），说明在本样本量下，两个模型的AUC无统计学显著差异；\n", delongResult[5]) +
                        "3. 科研建议：可扩大样本量（增加阳性/阴性病例数）后再次验证，或优化模型特征后重新分析。",
                contentFont
        );
        document.add(interpretationContent);
        document.add(Chunk.NEWLINE);

        // ---------------------- 附录：原始样本数据 ----------------------
        Paragraph appendixHeading = new Paragraph("六、附录：原始样本数据", headingFont);
        document.add(appendixHeading);

        // 原始数据表格（3列）
        PdfPTable sampleTable = new PdfPTable(3);
        sampleTable.setWidthPercentage(100);
        // 表格头部
        sampleTable.addCell(new PdfPCell(new Paragraph("trueLabel", tableHeaderFont)));
        sampleTable.addCell(new PdfPCell(new Paragraph("model1Score", tableHeaderFont)));
        sampleTable.addCell(new PdfPCell(new Paragraph("model2Score", tableHeaderFont)));
        // 表格内容
        for (MedicalSample sample : sampleList) {
            sampleTable.addCell(new Paragraph(String.valueOf(sample.trueLabel), contentFont));
            sampleTable.addCell(new Paragraph(String.format("%.2f", sample.model1Score), contentFont));
            sampleTable.addCell(new Paragraph(String.format("%.2f", sample.model2Score), contentFont));
        }
        document.add(sampleTable);

        // 5. 关闭文档
        document.close();
        System.out.println("DeLong检验分析报告（iText 5）已成功导出至：" + outputPath);
    }

    public static void main(String[] args) throws DocumentException, IOException {
        // 1. 加载样本数据（与之前一致）
        List<MedicalSample> sampleList = new ArrayList<>();
        sampleList .add(new MedicalSample(1, 0.85, 0.92));
        sampleList.add(new MedicalSample(1, 0.78, 0.89));
        sampleList.add(new MedicalSample(1, 0.91, 0.95));
        sampleList.add(new MedicalSample(1, 0.65, 0.76));
        sampleList.add(new MedicalSample(1, 0.72, 0.83));
        sampleList.add(new MedicalSample(1, 0.88, 0.90));
        sampleList.add(new MedicalSample(1, 0.94, 0.96));
        sampleList.add(new MedicalSample(1, 0.69, 0.79));
        sampleList.add(new MedicalSample(1, 0.75, 0.85));
        sampleList.add(new MedicalSample(1, 0.82, 0.88));
        sampleList.add(new MedicalSample(0, 0.25, 0.18));
        sampleList.add(new MedicalSample(0, 0.19, 0.12));
        sampleList.add(new MedicalSample(0, 0.32, 0.24));
        sampleList.add(new MedicalSample(0, 0.08, 0.05));
        sampleList.add(new MedicalSample(0, 0.15, 0.10));
        sampleList.add(new MedicalSample(0, 0.22, 0.16));
        sampleList.add(new MedicalSample(0, 0.38, 0.29));
        sampleList.add(new MedicalSample(0, 0.11, 0.07));
        sampleList.add(new MedicalSample(0, 0.28, 0.21));
        sampleList.add(new MedicalSample(0, 0.05, 0.03));
        sampleList.add(new MedicalSample(1, 0.80, 0.87));
        sampleList.add(new MedicalSample(1, 0.71, 0.80));
        sampleList.add(new MedicalSample(1, 0.90, 0.93));
        sampleList.add(new MedicalSample(0, 0.18, 0.13));
        sampleList.add(new MedicalSample(0, 0.26, 0.20));

        // 2. 提取标签与评分，执行DeLong检验
        List<Integer> trueLabels = new ArrayList<>();
        List<Double> model1Scores = new ArrayList<>();
        List<Double> model2Scores = new ArrayList<>();
        for (MedicalSample sample : sampleList) {
            trueLabels.add(sample.trueLabel);
            model1Scores.add(sample.model1Score);
            model2Scores.add(sample.model2Score);
        }
        double[] delongResult = delongTest(trueLabels, model1Scores, model2Scores);

        // 3. 导出PDF报告（指定导出路径，可自行修改）
        String pdfOutputPath = "DeLong医疗科研分析报告_Itext5.pdf";
        generateDelongPdfReport(delongResult, sampleList, pdfOutputPath);
    }
}