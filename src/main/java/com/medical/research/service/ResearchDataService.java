package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.research.ResearchData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ResearchDataService extends IService<ResearchData> {

    String importCsvData(Long experimentId, MultipartFile file) throws Exception;
    Page<ResearchData> getPageList(Long experimentId, Integer pageNum, Integer pageSize);
}