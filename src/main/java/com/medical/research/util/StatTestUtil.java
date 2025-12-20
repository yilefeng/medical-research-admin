package com.medical.research.util;

import java.util.List;
import java.util.Map;

public class StatTestUtil {
    public static double calculateAUC(List<Integer> labels, List<Double> scores) {
        List<Double> positiveScores = new java.util.ArrayList<>();
        List<Double> negativeScores = new java.util.ArrayList<>();

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

    public static double[] delongTest(List<Integer> labels, List<Double> scores1, List<Double> scores2) {
        List<Double> positiveScores1 = new java.util.ArrayList<>();
        List<Double> negativeScores1 = new java.util.ArrayList<>();
        List<Double> positiveScores2 = new java.util.ArrayList<>();
        List<Double> negativeScores2 = new java.util.ArrayList<>();

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

        double[][] covMatrix = new double[2][2];
        double var1 = 0.0;
        for (double posScore : positiveScores1) {
            double rank = 0.0;
            for (double score : scores1) {
                if (score < posScore) rank += 1.0;
                else if (score == posScore) rank += 0.5;
            }
            var1 += Math.pow((rank / n - auc1), 2);
        }
        var1 /= (m - 1);
        for (double negScore : negativeScores1) {
            double rank = 0.0;
            for (double score : scores1) {
                if (score < negScore) rank += 1.0;
                else if (score == negScore) rank += 0.5;
            }
            var1 += Math.pow(((m - rank / n) / m - auc1), 2);
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
            var2 += Math.pow((rank / n - auc2), 2);
        }
        var2 /= (m - 1);
        for (double negScore : negativeScores2) {
            double rank = 0.0;
            for (double score : scores2) {
                if (score < negScore) rank += 1.0;
                else if (score == negScore) rank += 0.5;
            }
            var2 += Math.pow(((m - rank / n) / m - auc2), 2);
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
        return x > 0 ? 1.0 - prob : prob;
    }

    public static Map<String, Object> delongTestWithRoc(List<Integer> labels, List<Double> scores1, List<Double> scores2) {
        // 原有DeLong检验逻辑
        double[] delongResult = delongTest(labels, scores1, scores2);
        double auc1 = delongResult[0];
        double auc2 = delongResult[1];
        double aucDiff = delongResult[2];
        double stdErr = delongResult[3];
        double zValue = delongResult[4];
        double pValue = delongResult[5];

        // 计算ROC数据
        double[][] rocData1 = RocChartUtil.calculateRocData(labels, scores1);
        double[][] rocData2 = RocChartUtil.calculateRocData(labels, scores2);

        // 封装结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("auc1", auc1);
        result.put("auc2", auc2);
        result.put("aucDiff", aucDiff);
        result.put("stdErr", stdErr);
        result.put("zValue", zValue);
        result.put("pValue", pValue);
        result.put("fpr1", rocData1[0]);
        result.put("tpr1", rocData1[1]);
        result.put("fpr2", rocData2[0]);
        result.put("tpr2", rocData2[1]);
        return result;
    }
}