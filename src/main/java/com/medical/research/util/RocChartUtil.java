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
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class RocChartUtil {
    @Value("${file.storage.dir:/data/download}")
    private String downloadDir;

    // 生成ROC曲线图（返回图片访问路径）
    public String generateRocChart(List<Double> fpr1, List<Double> tpr1, double auc1,
                                   List<Double> fpr2, List<Double> tpr2, double auc2,
                                   String reportName) throws Exception {
        // 创建图片存储目录
        String imgDir = downloadDir + "/" + "roc";
        File dir = new File(imgDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 图片文件名
        String imgFileName = "ROC曲线_" + reportName + "_" + System.currentTimeMillis() + ".png";
        String imgFullPath = imgDir + "/" + imgFileName;

        // 构建数据集
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series1 = new XYSeries("模型1 (AUC=" + String.format("%.4f", auc1) + ")");
        XYSeries series2 = new XYSeries("模型2 (AUC=" + String.format("%.4f", auc2) + ")");
        XYSeries baseline = new XYSeries("随机猜测");

        // 添加模型1数据（确保按FPR升序排列）
        List<Point2D> points1 = new ArrayList<>();
        for (int i = 0; i < fpr1.size(); i++) {
            points1.add(new Point2D.Double(fpr1.get(i), tpr1.get(i)));
        }
        points1.sort(Comparator.comparing(Point2D::getX));
        for (Point2D point : points1) {
            series1.add(point.getX(), point.getY());
        }

        // 添加模型2数据（确保按FPR升序排列）
        List<Point2D> points2 = new ArrayList<>();
        for (int i = 0; i < fpr2.size(); i++) {
            points2.add(new Point2D.Double(fpr2.get(i), tpr2.get(i)));
        }
        points2.sort(Comparator.comparing(Point2D::getX));
        for (Point2D point : points2) {
            series2.add(point.getX(), point.getY());
        }

        // 确保基线正确显示
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

        // 设置中文字体支持
        Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
        Font labelFont = new Font("微软雅黑", Font.PLAIN, 12);
        Font legendFont = new Font("微软雅黑", Font.PLAIN, 11);

        // 应用字体到图表元素
        chart.getTitle().setFont(titleFont);
        chart.getXYPlot().getDomainAxis().setLabelFont(labelFont);
        chart.getXYPlot().getRangeAxis().setLabelFont(labelFont);
        chart.getXYPlot().getDomainAxis().setTickLabelFont(labelFont);
        chart.getXYPlot().getRangeAxis().setTickLabelFont(labelFont);
        chart.getLegend().setItemFont(legendFont);

        // 美化图表
        XYPlot plot = chart.getXYPlot();
        plot.getRenderer().setSeriesPaint(0, Color.decode("#1989fa")); // 模型1蓝色
        plot.getRenderer().setSeriesPaint(1, Color.decode("#f56c6c")); // 模型2红色
        plot.getRenderer().setSeriesPaint(2, Color.decode("#909399")); // 基线灰色
        plot.getRenderer().setSeriesStroke(2, new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0)); // 虚线样式

        // 设置坐标轴范围确保ROC曲线正确显示
        plot.getDomainAxis().setRange(0.0, 1.0);
        plot.getRangeAxis().setRange(0.0, 1.0);

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

    // 反转评分列表的方法
    public static List<Double> invertScores(List<Double> scores) {
        List<Double> inverted = new ArrayList<>();
        double maxScore = scores.stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double minScore = scores.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double range = maxScore - minScore;

        for (Double score : scores) {
            if (range > 0) {
                inverted.add(maxScore - score + minScore);
            } else {
                inverted.add(1.0 - score);
            }
        }
        return inverted;
    }
}