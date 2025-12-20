package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.DataSource;
import com.medical.research.dto.datasource.DataSourceReqDTO;
import com.medical.research.dto.datasource.DataSourceRespDTO;
import com.medical.research.dto.datasource.DbTestReqDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

public interface DataSourceService extends IService<DataSource> {

    /**
     * 分页查询数据源
     */
    Page<DataSourceRespDTO> getSourcePage(DataSourceReqDTO req);

    /**
     * 新增Excel数据源
     */
    boolean addExcelSource(String sourceName, String filePath);

    /**
     * 新增数据库数据源
     */
    boolean addDbSource(DataSourceReqDTO req);

    /**
     * 修改数据库数据源
     */
    boolean updateDbSource(DataSourceReqDTO req);

    /**
     * 删除数据源
     */
    boolean deleteSource(Long id);

    /**
     * 根据ID查询数据源
     */
    DataSourceRespDTO getSourceById(Long id);

    /**
     * 测试数据库连接
     */
    boolean testDbConnection(DbTestReqDTO req);

    /**
     * 上传Excel文件
     */
    String uploadExcelFile(MultipartFile file);
}