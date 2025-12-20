package com.medical.research.util;

import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class RocChartUtil {
    @Value("${custom.pdf.save-path:/data1/pdf}")
    private String pdfSavePath;
    @Value("${custom.pdf.access-path:/data1/access}")
    private String pdfAccessPath;

    // 生成ROC曲线图（返回图片访问路径）
    public String generateRocChart(List<Double> fpr1, List<Double> tpr1, double auc1,
                                   List<Double> fpr2, List<Double> tpr2, double auc2,
                                   String reportName) throws Exception {
        // 创建图片存储目录
        String imgDir = pdfSavePath + "roc/";
        File dir = new File(imgDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 图片文件名
        String imgFileName = "ROC曲线_" + reportName + "_" + System.currentTimeMillis() + ".png";
        String imgFullPath = imgDir + imgFileName;
//        String imgAccessPath = pdfAccessPath + "roc/" + imgFileName;

        // 构建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("模型1 (AUC=" + String.format("%.4f", auc1) + ")");
        XYSeries series2 = new XYSeries("模型2 (AUC=" + String.format("%.4f", auc2) + ")");
        XYSeries baseline = new XYSeries("随机猜测");

        // 添加模型1数据
        for (int i = 0; i < fpr1.size(); i++) {
            series1.add(fpr1.get(i), tpr1.get(i));
        }
        // 添加模型2数据
        for (int i = 0; i < fpr2.size(); i++) {
            series2.add(fpr2.get(i), tpr2.get(i));
        }
        // 添加基线（对角线）
        baseline.add(0.0, 0.0);
        baseline.add(1.0, 1.0);

        dataset.addSeries(series1);
        dataset.addSeries(series2);
        dataset.addSeries(baseline);

        // 创建ROC图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                "ROC曲线（受试者工作特征曲线）",
                "假阳性率（FPR）",
                "真阳性率（TPR）",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        // 美化图表
        XYPlot plot = chart.getXYPlot();
        plot.getRenderer().setSeriesPaint(0, Color.decode("#1989fa"));
        plot.getRenderer().setSeriesPaint(1, Color.decode("#f56c6c"));
        plot.getRenderer().setSeriesPaint(2, Color.decode("#909399"));
        plot.getRenderer().setSeriesStroke(2, new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));

        // 保存图片
        ChartUtils.saveChartAsPNG(new File(imgFullPath), chart, 800, 600);
        log.info("ROC图片生成成功：{}", imgFullPath);
        return imgFullPath;
    }

    // 计算ROC曲线的FPR和TPR
    public static double[][] calculateRocData(List<Integer> labels, List<Double> scores) {
        // 去重并排序阈值
        List<Double> thresholds = new java.util.ArrayList<>(new java.util.HashSet<>(scores));
        thresholds.sort(java.util.Collections.reverseOrder());
        // 添加0和1作为边界阈值
        thresholds.add(0.0);
        thresholds.add(1.0);
        java.util.Collections.sort(thresholds, java.util.Collections.reverseOrder());

        List<Double> fprList = new java.util.ArrayList<>();
        List<Double> tprList = new java.util.ArrayList<>();

        int positiveCount = (int) labels.stream().filter(label -> label == 1).count();
        int negativeCount = labels.size() - positiveCount;

        for (double threshold : thresholds) {
            int tp = 0, fp = 0;
            for (int i = 0; i < labels.size(); i++) {
                if (scores.get(i) >= threshold) {
                    if (labels.get(i) == 1) {
                        tp++;
                    } else {
                        fp++;
                    }
                }
            }
            double tpr = positiveCount == 0 ? 0 : (double) tp / positiveCount;
            double fpr = negativeCount == 0 ? 0 : (double) fp / negativeCount;
            fprList.add(fpr);
            tprList.add(tpr);
        }

        // 排序（按FPR升序）
        java.util.List<Double[]> rocPoints = new java.util.ArrayList<>();
        for (int i = 0; i < fprList.size(); i++) {
            rocPoints.add(new Double[]{fprList.get(i), tprList.get(i)});
        }
        rocPoints.sort(Comparator.comparing(a -> a[0]));

        // 提取排序后的FPR和TPR
        double[] fpr = new double[rocPoints.size()];
        double[] tpr = new double[rocPoints.size()];
        for (int i = 0; i < rocPoints.size(); i++) {
            fpr[i] = (rocPoints.get(i)[0]);
            tpr[i] = rocPoints.get(i)[1];
        }
        return new double[][]{fpr, tpr};
    }
}