package com.medical.research.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson2.JSON;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.ResearchData;
import com.medical.research.service.ResearchDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 科研数据Excel导入监听器
 * 用于EasyExcel解析Excel文件，批量处理科研数据导入
 */
@Slf4j
public class ResearchDataExcelListener extends AnalysisEventListener<ResearchData> {

    /**
     * 批量插入阈值（每100条插入一次）
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 临时存储解析的数据
     */
    private List<ResearchData> dataList = new ArrayList<>(BATCH_COUNT);

    /**
     * 科研数据服务（通过构造器注入，避免Spring容器管理问题）
     */
    private final ResearchDataService researchDataService;

    /**
     * 导入失败的错误信息
     */
    private String errorMsg = "";

    /**
     * 构造器注入Service
     * @param researchDataService 科研数据服务
     */
    public ResearchDataExcelListener(ResearchDataService researchDataService) {
        this.researchDataService = researchDataService;
    }

    /**
     * 解析每一行数据时调用
     * @param data 解析后的单行数据
     * @param context 解析上下文
     */
    @Override
    public void invoke(ResearchData data, AnalysisContext context) {
        log.debug("解析到一行数据:{}", JSON.toJSONString(data));

        // 数据校验
        String validateMsg = validateData(data, context.readRowHolder().getRowIndex() + 1);
        if (StringUtils.hasText(validateMsg)) {
            errorMsg = validateMsg;
            throw new RuntimeException(validateMsg);
        }

        // 补充默认字段
        data.setCreateTime(LocalDateTime.now());
        data.setUpdateTime(LocalDateTime.now());

        // 添加到临时列表
        data.setId(null);
        dataList.add(data);

        // 达到批量阈值时插入数据库
        if (dataList.size() >= BATCH_COUNT) {
            saveData();
            // 清空列表释放内存
            dataList.clear();
        }
    }

    /**
     * 所有数据解析完成后调用
     * @param context 解析上下文
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余数据
        saveData();
        log.info("Excel文件解析完成，共处理{}条数据", dataList.size() + (context.readRowHolder().getRowIndex() - 1));
    }

    /**
     * 数据转换异常处理（如类型不匹配）
     * @param exception 转换异常
     * @param context 解析上下文
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("Excel数据解析异常", exception);

        // 数据转换异常处理
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
            errorMsg = String.format(
                    "第%s行第%s列数据格式错误，值为：%s，错误原因：%s",
                    convertException.getRowIndex() + 1,
                    convertException.getColumnIndex() + 1,
                    convertException.getCellData(),
                    convertException.getMessage()
            );
        } else {
            errorMsg = "Excel解析失败：" + exception.getMessage();
        }

        throw new RuntimeException(errorMsg);
    }

    /**
     * 批量保存数据到数据库
     */
    private void saveData() {
        if (!dataList.isEmpty()) {
            log.info("批量插入{}条科研数据", dataList.size());
            try {
                boolean success = researchDataService.saveBatch(dataList);
                if (!success) {
                    throw new RuntimeException("批量插入科研数据失败");
                }
            } catch (Exception e) {
                log.error("批量插入科研数据异常", e);
                throw new RuntimeException("批量插入数据失败：" + e.getMessage());
            }
        }
    }

    /**
     * 数据有效性校验
     * @param data 待校验数据
     * @param rowNum 行号（前端展示用）
     * @return 校验失败信息（空字符串表示校验通过）
     */
    private String validateData(ResearchData data, int rowNum) {
        // 1. 实验编号校验
        if (!StringUtils.hasText(data.getExperimentNo())) {
            return String.format("第%s行：实验编号不能为空", rowNum);
        }

        // 2. 模型名称校验
        if (!StringUtils.hasText(data.getModelName())) {
            return String.format("第%s行：模型名称不能为空", rowNum);
        }

        // 3. 数据集名称校验
        if (!StringUtils.hasText(data.getDataset())) {
            return String.format("第%s行：数据集名称不能为空", rowNum);
        }

        // 4. 数值字段校验（准确率/精确率/召回率/F1分数需在0-1之间）
        if (data.getAccuracy() == null || data.getAccuracy().doubleValue() < 0 || data.getAccuracy().doubleValue() > 1) {
            return String.format("第%s行：准确率必须为0-1之间的数值", rowNum);
        }
        if (data.getPrecision() == null || data.getPrecision().doubleValue() < 0 || data.getPrecision().doubleValue() > 1) {
            return String.format("第%s行：精确率必须为0-1之间的数值", rowNum);
        }
        if (data.getRecall() == null || data.getRecall().doubleValue() < 0 || data.getRecall().doubleValue() > 1) {
            return String.format("第%s行：召回率必须为0-1之间的数值", rowNum);
        }
        if (data.getF1Score() == null || data.getF1Score().doubleValue() < 0 || data.getF1Score().doubleValue() > 1) {
            return String.format("第%s行：F1分数必须为0-1之间的数值", rowNum);
        }

        // 校验通过
        return "";
    }

    /**
     * 获取导入错误信息
     * @return 错误信息
     */
    public String getErrorMsg() {
        return errorMsg;
    }
}