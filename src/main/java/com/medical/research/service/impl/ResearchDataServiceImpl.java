package com.medical.research.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.dto.research.ResearchDataReqDTO;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.ResearchData;
import com.medical.research.listener.ResearchDataExcelListener;
import com.medical.research.mapper.ResearchDataMapper;
import com.medical.research.service.ResearchDataService;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResearchDataServiceImpl extends ServiceImpl<ResearchDataMapper, ResearchData>
        implements ResearchDataService {

    @Resource
    private ResearchDataMapper researchDataMapper;

    @Override
    public Page<ResearchDataRespDTO> getDataPage(ResearchDataReqDTO req) {
        Page<ResearchDataRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = researchDataMapper.selectDataCount(req);
        page.setTotal(total);

        // 查询列表
        List<ResearchData> dataList = researchDataMapper.selectDataPage(req);
        List<ResearchDataRespDTO> respList = dataList.stream().map(data -> {
            ResearchDataRespDTO resp = new ResearchDataRespDTO();
            BeanUtils.copyProperties(data, resp);
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addData(ResearchDataReqDTO req) {
        ResearchData data = new ResearchData();
        BeanUtils.copyProperties(req, data);
        data.setCreateTime(LocalDateTime.now());
        data.setUpdateTime(LocalDateTime.now());
        return save(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateData(ResearchDataReqDTO req) {
        ResearchData data = getById(req.getId());
        if (data == null) {
            return false;
        }
        BeanUtils.copyProperties(req, data);
        data.setUpdateTime(LocalDateTime.now());
        return updateById(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteData(Long id) {
        return removeById(id);
    }

    @Override
    public ResearchDataRespDTO getDataById(Long id) {
        ResearchData data = getById(id);
        if (data == null) {
            return null;
        }
        ResearchDataRespDTO resp = new ResearchDataRespDTO();
        BeanUtils.copyProperties(data, resp);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<ResearchData> dataList = new ArrayList<>();
            // 跳过表头，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                ResearchData data = new ResearchData();
                // 实验编号
                Cell cell0 = row.getCell(0);
                if (cell0 != null) {
                    data.setExperimentNo(cell0.getStringCellValue());
                }
                // 模型名称
                Cell cell1 = row.getCell(1);
                if (cell1 != null) {
                    data.setModelName(cell1.getStringCellValue());
                }
                // 数据集
                Cell cell2 = row.getCell(2);
                if (cell2 != null) {
                    data.setDataset(cell2.getStringCellValue());
                }
                // 准确率
                Cell cell3 = row.getCell(3);
                if (cell3 != null) {
                    data.setAccuracy(BigDecimal.valueOf(cell3.getNumericCellValue()));
                }
                // 精确率
                Cell cell4 = row.getCell(4);
                if (cell4 != null) {
                    data.setPrecision(BigDecimal.valueOf(cell4.getNumericCellValue()));
                }
                // 召回率
                Cell cell5 = row.getCell(5);
                if (cell5 != null) {
                    data.setRecall(BigDecimal.valueOf(cell5.getNumericCellValue()));
                }
                // F1分数
                Cell cell6 = row.getCell(6);
                if (cell6 != null) {
                    data.setF1Score(BigDecimal.valueOf(cell6.getNumericCellValue()));
                }

                data.setCreateTime(LocalDateTime.now());
                data.setUpdateTime(LocalDateTime.now());
                dataList.add(data);
            }

            // 批量插入
            return saveBatch(dataList);
        } catch (Exception e) {
            log.error("Excel导入科研数据失败", e);
            return false;
        }
    }

    @Override
    public List<ResearchDataRespDTO> getDataByExperimentNo(String experimentNo) {
        List<ResearchData> dataList = researchDataMapper.selectDataByExperimentNo(experimentNo);
        return dataList.stream().map(data -> {
            ResearchDataRespDTO resp = new ResearchDataRespDTO();
            BeanUtils.copyProperties(data, resp);
            return resp;
        }).collect(Collectors.toList());
    }

    /**
     * EasyExcel导入科研数据（推荐使用，性能更优）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importDataByEasyExcel(MultipartFile file) {
        try {
            // 创建监听器（注入当前service）
            ResearchDataExcelListener listener = new ResearchDataExcelListener(this);

            // 解析Excel文件
            EasyExcel.read(file.getInputStream(), ResearchData.class, listener)
                    .sheet() // 读取第一个sheet
                    .headRowNumber(1) // 表头行号（第1行是表头）
                    .doRead();

            // 检查是否有导入错误
            if (StringUtils.hasText(listener.getErrorMsg())) {
                log.error("Excel导入失败：{}", new Exception(listener.getErrorMsg()));
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("EasyExcel导入科研数据失败", e);
            return false;
        }
    }
}