// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.medical.research.entity.ResearchData;
import com.medical.research.entity.StatModel;
import com.medical.research.dto.stat.StatModelReqDTO;
import com.medical.research.dto.stat.StatModelRespDTO;

import java.util.List;

public interface StatModelService extends IService<StatModel> {

    /**
     * 查询所有启用的统计模型
     */
    List<StatModelRespDTO> getEnableModels();

    /**
     * 添加统计模型
     */
    boolean addModel(StatModelReqDTO req);

    /**
     * 修改统计模型
     */
    boolean updateModel(StatModelReqDTO req);

    /**
     * 【新增核心方法】- 适配真实科研数据传入
     * @param modelCode 模型编码（t_test/chi_square等）
     * @param statConditions 统计条件（JSON格式，可选）
     * @param researchDataList 科研数据列表（从research_data表查询的真实数据）
     * @return 统计报告内容（HTML格式）
     */
    String executeModel(String modelCode, String statConditions, List<ResearchDataRespDTO> researchDataList);

}

