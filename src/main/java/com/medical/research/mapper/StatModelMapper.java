package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.StatModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StatModelMapper extends BaseMapper<StatModel> {

    /**
     * 查询启用的统计模型
     */
    List<StatModel> selectEnableModels();

    /**
     * 根据编码查询模型
     */
    StatModel selectByCode(@Param("modelCode") String modelCode);
}