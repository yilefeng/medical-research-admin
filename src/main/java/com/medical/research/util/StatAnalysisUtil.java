package com.medical.research.util;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 统计分析工具类：基于Apache Commons Math实现t检验、卡方检验等
 */
@Component
public class StatAnalysisUtil {
    private final TTest tTest = new TTest();
    private final ChiSquareTest chiSquareTest = new ChiSquareTest();
    private final SimpleRegression linearRegression = new SimpleRegression();
    private final OneWayAnova anova = new OneWayAnova();

    /**
     * t检验（两组数据均值比较）
     */
    public Map<String, Object> tTestAnalysis(double[] sample1, double[] sample2, boolean paired) {
        double pValue;
        if (paired) {
            // 配对t检验
            pValue = tTest.pairedTTest(sample1, sample2);
        } else {
            // 独立样本t检验
            pValue = tTest.tTest(sample1, sample2);
        }

        boolean significant = pValue < 0.05; // α=0.05
        double mean1 = Arrays.stream(sample1).average().orElse(0);
        double mean2 = Arrays.stream(sample2).average().orElse(0);

        // JDK 8 兼容写法
        Map<String, Object> result = new HashMap<>();
        result.put("检验类型", paired ? "配对t检验" : "独立样本t检验");
        result.put("p值", String.format("%.4f", pValue));
        result.put("是否显著", significant);
        result.put("样本1均值", String.format("%.4f", mean1));
        result.put("样本2均值", String.format("%.4f", mean2));
        result.put("结论", significant ? "两组数据存在显著差异" : "两组数据无显著差异");
        return Collections.unmodifiableMap(result);
    }

    /**
     * 卡方检验（分类数据独立性检验）
     */
    public Map<String, Object> chiSquareTestAnalysis(long[][] observed) {
        double chiSquare = chiSquareTest.chiSquare(observed);
        double pValue = chiSquareTest.chiSquareTest(observed);
        boolean significant = pValue < 0.05;
        int df = observed.length - 1; // 自由度

        // JDK 8 兼容写法
        Map<String, Object> result = new HashMap<>();
        result.put("卡方值", String.format("%.4f", chiSquare));
        result.put("p值", String.format("%.4f", pValue));
        result.put("自由度", df);
        result.put("是否显著", significant);
        result.put("结论", significant ? "分类变量存在关联" : "分类变量无关联");
        return Collections.unmodifiableMap(result);
    }

    /**
     * 线性回归分析
     */
    public Map<String, Object> linearRegAnalysis(double[][] data) {
        linearRegression.clear(); // 清空之前的数据
        for (double[] point : data) {
            linearRegression.addData(point[0], point[1]); // x:自变量，y:因变量
        }

        double slope = linearRegression.getSlope(); // 斜率
        double intercept = linearRegression.getIntercept(); // 截距
        double rSquare = linearRegression.getRSquare(); // 决定系数
        String relation = rSquare > 0.8 ? "强" : (rSquare > 0.5 ? "中等" : "弱");

        // JDK 8 兼容写法
        Map<String, Object> result = new HashMap<>();
        result.put("斜率", String.format("%.4f", slope));
        result.put("截距", String.format("%.4f", intercept));
        result.put("决定系数(R²)", String.format("%.4f", rSquare));
        result.put("线性关系强度", relation);
        result.put("回归方程", String.format("y = %.4fx + %.4f", slope, intercept));
        return Collections.unmodifiableMap(result);
    }

    /**
     * 方差分析（多组数据差异检验）
     */
    public Map<String, Object> anovaAnalysis(List<double[]> groups) {
        double pValue = anova.anovaPValue(groups);
        boolean significant = pValue < 0.05;

        // JDK 8 兼容写法
        Map<String, Object> result = new HashMap<>();
        result.put("p值", String.format("%.4f", pValue));
        result.put("是否显著", significant);
        result.put("组数", groups.size());
        result.put("结论", significant ? "多组数据存在显著差异" : "多组数据无显著差异");
        return Collections.unmodifiableMap(result);
    }

}