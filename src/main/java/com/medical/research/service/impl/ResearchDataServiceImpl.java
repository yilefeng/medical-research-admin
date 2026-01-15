package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.entity.research.ResearchData;
import com.medical.research.mapper.ResearchDataMapper;
import com.medical.research.service.ResearchDataService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResearchDataServiceImpl extends ServiceImpl<ResearchDataMapper, ResearchData> implements ResearchDataService {

    @Override
    public String importCsvData(Long experimentId, MultipartFile file) throws Exception {
        List<ResearchData> dataList = new ArrayList<>();
        // 解析CSV文件
        try (CSVParser parser = new CSVParser(
                new InputStreamReader(file.getInputStream()),
                CSVFormat.DEFAULT.withHeader("true_label", "model1_score", "model2_score").withSkipHeaderRecord()
        )) {
            for (CSVRecord record : parser) {
                ResearchData data = new ResearchData();
                data.setExperimentId(experimentId);
                data.setTrueLabel(Integer.parseInt(record.get("true_label")));
                data.setModel1Score(Double.parseDouble(record.get("model1_score")));
                data.setModel2Score(Double.parseDouble(record.get("model2_score")));
                data.setDataSource(file.getOriginalFilename());
                data.setCreateTime(LocalDateTime.now());
                dataList.add(data);
            }
        }
        // 批量插入
        this.saveBatch(dataList);
        return "共导入" + dataList.size() + "条数据";
    }

    @Override
    public Page<ResearchData> getPageList(List<Long> experimentIdList, Integer pageNum, Integer pageSize) {
        if (experimentIdList == null || experimentIdList.isEmpty()) {
            return new Page<>();
        }
        Page<ResearchData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResearchData> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ResearchData::getExperimentId, experimentIdList);
        wrapper.orderByDesc(ResearchData::getId);
        return this.page(page, wrapper);
    }
}