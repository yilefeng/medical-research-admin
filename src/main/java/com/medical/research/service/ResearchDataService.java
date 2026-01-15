package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.research.ResearchData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResearchDataService extends IService<ResearchData> {

    String importCsvData(Long experimentId, MultipartFile file) throws Exception;
    Page<ResearchData> getPageList(List<Long> experimentIdList, Integer pageNum, Integer pageSize);
}