package com.medical.research.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.entity.experiment.ExperimentResearcher;
import com.medical.research.mapper.ExperimentResearcherMapper;
import com.medical.research.service.ExperimentResearcherService;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/12 15:02
 * @Description:
 */
@Service
public class ExperimentResearcherServiceImpl extends ServiceImpl<ExperimentResearcherMapper, ExperimentResearcher> implements ExperimentResearcherService {

    @Override
    public void saveResearcher(Long experimentId, List<Long> researcherIds) {
        List<ExperimentResearcher> existingList = list(new LambdaQueryWrapper<ExperimentResearcher>()
                .eq(ExperimentResearcher::getExperimentId, experimentId));

        for (ExperimentResearcher existing : existingList) {
            if (!researcherIds.contains(existing.getResearcherId())) {
                existing.setStatus(ExperimentResearcher.Status.DELETED.getValue());
            } else {
                existing.setStatus(ExperimentResearcher.Status.NORMAL.getValue());
            }
        }

        Set<Long> existingResearcherIds = existingList.stream()
                .map(ExperimentResearcher::getResearcherId)
                .collect(Collectors.toSet());

        // 找出需要新增的记录（存在于目标列表但不在现有列表中的）
        List<ExperimentResearcher> toAdd = researcherIds.stream()
                .filter(researcherId -> !existingResearcherIds.contains(researcherId))
                .map(researcherId -> {
                    ExperimentResearcher entity = new ExperimentResearcher();
                    entity.setExperimentId(experimentId);
                    entity.setResearcherId(researcherId);
                    entity.setStatus(ExperimentResearcher.Status.NORMAL.getValue());
                    return entity;
                })
                .collect(Collectors.toList());

        List<ExperimentResearcher> list = new ArrayList<>();
        // 更新需要修改状态的记录
        if (!existingList.isEmpty()) {
            list.addAll(existingList);
        }

        // 新增不存在的记录
        if (!toAdd.isEmpty()) {
            list.addAll(toAdd);
        }

        saveOrUpdateBatch(list);
    }
}
