package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.ResearchData;
import com.medical.research.dto.research.ResearchDataReqDTO;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResearchDataService extends IService<ResearchData> {

    String importCsvData(Long experimentId, MultipartFile file) throws Exception;
    Page<ResearchData> getPageList(Long experimentId, Integer pageNum, Integer pageSize);
}