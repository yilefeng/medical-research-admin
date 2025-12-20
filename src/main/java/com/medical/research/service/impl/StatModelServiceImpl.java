package com.medical.research.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.ResearchData;
import com.medical.research.entity.StatModel;
import com.medical.research.mapper.StatModelMapper;
import com.medical.research.dto.stat.StatModelReqDTO;
import com.medical.research.dto.stat.StatModelRespDTO;
import com.medical.research.service.StatModelService;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatModelServiceImpl extends ServiceImpl<StatModelMapper, StatModel>
        implements StatModelService {

    @Resource
    private StatModelMapper statModelMapper;

    // 统计工具类
    private final TTest tTest = new TTest();
    private final ChiSquareTest chiSquareTest = new ChiSquareTest();
    private final SimpleRegression simpleRegression = new SimpleRegression();
    private final OneWayAnova oneWayAnova = new OneWayAnova();

    // ========== 新增核心方法（适配真实数据） ==========
    @Override
    public String executeModel(String modelCode, String statConditions, List<ResearchDataRespDTO> researchDataList) {
        // 1. 基础校验
        if (CollectionUtils.isEmpty(researchDataList)) {
            return "<h3>统计失败</h3><p>无可用的科研数据，请先导入数据</p>";
        }
        if (!StringUtils.hasText(modelCode)) {
            return "<h3>统计失败</h3><p>未指定统计模型</p>";
        }

        // 2. 解析统计条件（JSON格式，可选）- 增强统计灵活性
        JSONObject conditionObj = new JSONObject();
        if (StringUtils.hasText(statConditions)) {
            try {
                conditionObj = JSON.parseObject(statConditions);
            } catch (Exception e) {
                return "<h3>统计失败</h3><p>统计条件格式错误：" + e.getMessage() + "</p>";
            }
        }

        // 3. 根据模型编码执行真实统计计算
        String reportContent = "";
        switch (modelCode) {
            case "t_test": // t检验（对比两组模型的准确率）
                reportContent = executeTTest(researchDataList, conditionObj);
                break;
            case "chi_square": // 卡方检验（分析数据集与准确率的关联性）
                reportContent = executeChiSquare(researchDataList, conditionObj);
                break;
            case "linear_regression": // 线性回归（准确率与召回率的回归分析）
                reportContent = executeLinearRegression(researchDataList, conditionObj);
                break;
            case "anova": // 方差分析（多模型准确率差异分析）
                reportContent = executeAnova(researchDataList, conditionObj);
                break;
            default:
                reportContent = "<h3>不支持的统计模型：</h3><p>" + modelCode + "</p>";
        }

        return reportContent;
    }

    // ========== 其他原有方法（不变） ==========
    @Override
    public List<StatModelRespDTO> getEnableModels() {
        List<StatModel> modelList = statModelMapper.selectEnableModels();
        return modelList.stream().map(model -> {
            StatModelRespDTO resp = new StatModelRespDTO();
            BeanUtils.copyProperties(model, resp);
            return resp;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addModel(StatModelReqDTO req) {
        StatModel existModel = statModelMapper.selectByCode(req.getModelCode());
        if (existModel != null) {
            return false;
        }
        StatModel model = new StatModel();
        BeanUtils.copyProperties(req, model);
        model.setCreateTime(LocalDateTime.now());
        model.setUpdateTime(LocalDateTime.now());
        return save(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateModel(StatModelReqDTO req) {
        StatModel model = getById(req.getId());
        if (model == null) {
            return false;
        }
        BeanUtils.copyProperties(req, model, "modelCode");
        model.setUpdateTime(LocalDateTime.now());
        return updateById(model);
    }

    // ========== 统计方法实现（新增conditionObj参数，增强灵活性） ==========
    /**
     * T检验：支持通过统计条件指定对比的两个模型
     */
    private String executeTTest(List<ResearchDataRespDTO> dataList, JSONObject conditionObj) {
        // 从统计条件中获取要对比的两个模型（默认取前两个）
        String model1 = conditionObj.getString("model1");
        String model2 = conditionObj.getString("model2");

        // 分组：按模型名称分组
        Map<String, List<ResearchDataRespDTO>> modelGroup = dataList.stream()
                .collect(Collectors.groupingBy(ResearchDataRespDTO::getModelName));

        // 处理自定义模型名称
        double[] group1, group2;
        if (StringUtils.hasText(model1) && StringUtils.hasText(model2)) {
            if (!modelGroup.containsKey(model1) || !modelGroup.containsKey(model2)) {
                return "<h3>t检验报告</h3><p>指定的模型数据不存在：" + model1 + " / " + model2 + "</p>";
            }
            group1 = modelGroup.get(model1).stream().mapToDouble(t->t.getAccuracy().doubleValue()).toArray();
            group2 = modelGroup.get(model2).stream().mapToDouble(t->t.getAccuracy().doubleValue()).toArray();
        } else {
            // 默认取前两组
            if (modelGroup.size() < 2) {
                return "<h3>t检验报告</h3><p>数据不足：至少需要两组不同模型的数据</p>";
            }
            List<String> modelNames = modelGroup.keySet().stream().limit(2).collect(Collectors.toList());
            model1 = modelNames.get(0);
            model2 = modelNames.get(1);
            group1 = modelGroup.get(model1).stream().mapToDouble(t->t.getAccuracy().doubleValue()).toArray();
            group2 = modelGroup.get(model2).stream().mapToDouble(t->t.getAccuracy().doubleValue()).toArray();
        }

        // 数据量验证（每组至少需要2个样本）
        if (group1.length < 2) {
            return "<h3>t检验报告</h3><p>数据不足：" + model1 + "组至少需要2个数据点</p>";
        }
        if (group2.length < 2) {
            return "<h3>t检验报告</h3><p>数据不足：" + model2 + "组至少需要2个数据点</p>";
        }

        // 执行t检验并处理异常
        try {
            double pValue = tTest.tTest(group1, group2);
            boolean isSignificant = pValue < 0.05;

            // 生成报告
            return String.format(
                    "<h3>独立样本t检验报告</h3>" +
                            "<p><strong>对比模型：</strong>%s vs %s</p>" +
                            "<p><strong>样本量：</strong>%s组=%d条，%s组=%d条</p>" +
                            "<p><strong>均值：</strong>%s组=%.4f，%s组=%.4f</p>" +
                            "<p><strong>p值：</strong>%.4f</p>" +
                            "<p><strong>显著性：</strong>%s（α=0.05）</p>" +
                            "<p><strong>结论：</strong>%s</p>",
                    model1, model2,
                    model1, group1.length, model2, group2.length,
                    model1, calculateMean(group1), model2, calculateMean(group2),
                    pValue,
                    isSignificant ? "显著" : "不显著",
                    isSignificant ? "两组模型的准确率存在显著差异" : "两组模型的准确率无显著差异"
            );
        } catch (Exception e) {
            return "<h3>t检验报告</h3><p>统计计算失败：" + e.getMessage() + "</p>";
        }
    }

    /**
     * 卡方检验：支持通过统计条件指定准确率阈值
     */
    private String executeChiSquare(List<ResearchDataRespDTO> dataList, JSONObject conditionObj) {
        // 从统计条件获取准确率阈值（默认0.9）
        double accuracyThreshold = conditionObj.getDoubleValue("accuracyThreshold");
        if (accuracyThreshold <= 0 || accuracyThreshold >= 1) {
            accuracyThreshold = 0.9;
        }

        final double ACCURACY_THRESHOLD = accuracyThreshold;
        // 数据离散化：按数据集分组，按阈值划分准确率
        Map<String, Map<String, Long>> contingencyTable = dataList.stream()
                .collect(Collectors.groupingBy(
                        ResearchDataRespDTO::getDataset,
                        Collectors.groupingBy(
                                d -> d.getAccuracy().doubleValue() >= ACCURACY_THRESHOLD ? "高准确率" : "低准确率",
                                Collectors.counting()
                        )
                ));

        // 构建列联表
        List<String> datasets = contingencyTable.keySet().stream().collect(Collectors.toList());
        long[][] observed = new long[datasets.size()][2];
        for (int i = 0; i < datasets.size(); i++) {
            Map<String, Long> data = contingencyTable.get(datasets.get(i));
            observed[i][0] = data.getOrDefault("高准确率", 0L);
            observed[i][1] = data.getOrDefault("低准确率", 0L);
        }

        // 执行卡方检验
        double chiSquare = chiSquareTest.chiSquare(observed);
        double pValue = chiSquareTest.chiSquareTest(observed);
        boolean isSignificant = pValue < 0.05;

        // 生成报告
        StringBuilder report = new StringBuilder("<h3>卡方检验报告（数据集 vs 准确率）</h3>");
        report.append("<p><strong>准确率阈值：</strong>").append(accuracyThreshold).append("</p>");
        report.append("<p><strong>卡方值：</strong>").append(String.format("%.4f", chiSquare)).append("</p>");
        report.append("<p><strong>p值：</strong>").append(String.format("%.4f", pValue)).append("</p>");
        report.append("<p><strong>显著性：</strong>").append(isSignificant ? "显著" : "不显著").append("（α=0.05）</p>");
        report.append("<p><strong>结论：</strong>").append(isSignificant ? "数据集与准确率存在显著关联" : "数据集与准确率无显著关联").append("</p>");

        // 补充列联表
        report.append("<h4>列联表（样本数）</h4>");
        report.append("<table border='1' cellpadding='5' cellspacing='0'>");
        report.append("<tr><th>数据集</th><th>高准确率</th><th>低准确率</th></tr>");
        for (int i = 0; i < datasets.size(); i++) {
            report.append("<tr>");
            report.append("<td>").append(datasets.get(i)).append("</td>");
            report.append("<td>").append(observed[i][0]).append("</td>");
            report.append("<td>").append(observed[i][1]).append("</td>");
            report.append("</tr>");
        }
        report.append("</table>");

        return report.toString();
    }

    /**
     * 线性回归：支持通过统计条件指定自变量（recall/precision/f1Score）
     */
    private String executeLinearRegression(List<ResearchDataRespDTO> dataList, JSONObject conditionObj) {
        // 从统计条件获取自变量（默认recall）
        String xField = conditionObj.getString("xField");
        if (!StringUtils.hasText(xField) || (!"recall".equals(xField) && !"precision".equals(xField) && !"f1Score".equals(xField))) {
            xField = "recall";
        }

        // 清空回归模型
        simpleRegression.clear();

        final String xFieldF = xField;
        // 添加数据点（x=指定字段，y=准确率）
        dataList.forEach(d -> {
            Double xValue;
            switch (xFieldF) {
                case "precision":
                    xValue = d.getPrecision().doubleValue();
                    break;
                case "f1Score":
                    xValue = d.getF1Score().doubleValue();
                    break;
                default:
                    xValue = d.getRecall().doubleValue();
                    break;
            }
            if (xValue != null && d.getAccuracy() != null) {
                simpleRegression.addData(xValue, d.getAccuracy().doubleValue());
            }
        });

        // 回归分析
        double slope = simpleRegression.getSlope(); // 斜率
        double intercept = simpleRegression.getIntercept(); // 截距
        double rSquare = simpleRegression.getRSquare(); // 决定系数R²
        double pValue = simpleRegression.getSignificance(); // 显著性p值
        boolean isSignificant = pValue < 0.05;

        // 生成报告
        String xFieldCn;
        switch (xFieldF) {
            case "precision":
                xFieldCn = "精确率";
                break;
            case "f1Score":
                xFieldCn = "F1分数";
                break;
            default:
                xFieldCn = "召回率";
                break;
        }

        return String.format(
                "<h3>线性回归分析报告（%s → 准确率）</h3>" +
                        "<p><strong>回归方程：</strong>准确率 = %.4f × %s + %.4f</p>" +
                        "<p><strong>决定系数R²：</strong>%.4f（解释力：%.1f%%）</p>" +
                        "<p><strong>斜率p值：</strong>%.4f</p>" +
                        "<p><strong>显著性：</strong>%s（α=0.05）</p>" +
                        "<p><strong>结论：</strong>%s</p>",
                xFieldCn,
                slope, xFieldCn, intercept,
                rSquare, rSquare * 100,
                pValue,
                isSignificant ? "显著" : "不显著",
                isSignificant ? String.format("%s每增加1个单位，准确率平均%s%.4f个单位", xFieldCn, slope > 0 ? "增加" : "减少", Math.abs(slope)) : xFieldCn + "与准确率无显著线性关系"
        );
    }

    /**
     * 方差分析：支持通过统计条件筛选模型
     */
    private String executeAnova(List<ResearchDataRespDTO> dataList, JSONObject conditionObj) {
        // 从统计条件获取要分析的模型列表（为空则分析所有）
        List<String> targetModels = conditionObj.getJSONArray("targetModels").stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        // 按模型名称分组
        Map<String, List<ResearchDataRespDTO>> modelGroup = dataList.stream()
                .filter(d -> CollectionUtils.isEmpty(targetModels) || targetModels.contains(d.getModelName()))
                .collect(Collectors.groupingBy(ResearchDataRespDTO::getModelName));

        if (modelGroup.size() < 2) {
            return "<h3>方差分析报告</h3><p>数据不足：至少需要两组不同模型的数据</p>";
        }

        // 转换为方差分析所需格式
        List<double[]> groups = modelGroup.values().stream()
                .map(list -> list.stream().mapToDouble(t->t.getAccuracy().doubleValue()).toArray())
                .collect(Collectors.toList());

        // 执行单因素方差分析
        double fValue = oneWayAnova.anovaFValue(groups);
        double pValue = oneWayAnova.anovaPValue(groups);
        boolean isSignificant = pValue < 0.05;

        // 计算各组均值
        StringBuilder meanStr = new StringBuilder();
        modelGroup.forEach((model, values) -> {
            double mean = values.stream().mapToDouble(t->t.getAccuracy().doubleValue()).average().orElse(0);
            meanStr.append(model).append("=").append(String.format("%.4f", mean)).append("，");
        });
        if (meanStr.length() > 0) {
            meanStr.deleteCharAt(meanStr.length() - 1);
        }

        // 生成报告
        return String.format(
                "<h3>单因素方差分析报告（模型 vs 准确率）</h3>" +
                        "<p><strong>参与分析的模型：</strong>%s</p>" +
                        "<p><strong>各组均值：</strong>%s</p>" +
                        "<p><strong>F值：</strong>%.4f</p>" +
                        "<p><strong>p值：</strong>%.4f</p>" +
                        "<p><strong>显著性：</strong>%s（α=0.05）</p>" +
                        "<p><strong>结论：</strong>%s</p>",
                String.join("、", modelGroup.keySet()),
                meanStr.toString(),
                fValue,
                pValue,
                isSignificant ? "显著" : "不显著",
                isSignificant ? "不同模型的准确率存在显著差异" : "不同模型的准确率无显著差异"
        );
    }

    /**
     * 计算数组均值
     */
    private double calculateMean(double[] data) {
        if (data.length == 0) {
            return 0;
        }
        double sum = 0;
        for (double d : data) {
            sum += d;
        }
        return sum / data.length;
    }
}