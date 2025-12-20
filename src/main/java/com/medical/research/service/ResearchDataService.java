package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.ResearchData;
import com.medical.research.dto.research.ResearchDataReqDTO;
import com.medical.research.dto.research.ResearchDataRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResearchDataService extends IService<ResearchData> {

    /**
     * 分页查询科研数据
     */
    Page<ResearchDataRespDTO> getDataPage(ResearchDataReqDTO req);

    /**
     * 新增科研数据
     */
    boolean addData(ResearchDataReqDTO req);

    /**
     * 修改科研数据
     */
    boolean updateData(ResearchDataReqDTO req);

    /**
     * 删除科研数据
     */
    boolean deleteData(Long id);

    /**
     * 根据ID查询科研数据
     */
    ResearchDataRespDTO getDataById(Long id);

    /**
     * Excel导入科研数据
     */
    boolean importData(MultipartFile file);

    /**
     * 根据实验编号查询数据（可视化用）
     */
    List<ResearchDataRespDTO> getDataByExperimentNo(String experimentNo);

    /**
     * EasyExcel导入科研数据
     */
    public boolean importDataByEasyExcel(MultipartFile file);
}