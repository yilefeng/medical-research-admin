package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.experiment.ExperimentPlan;
import com.medical.research.entity.experiment.ExperimentResearcher;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/12 15:00
 * @Description:
 */
public interface ExperimentResearcherService extends IService<ExperimentResearcher> {

     void saveResearcher(Long experimentId, List<Long>  researcherIds);
}
