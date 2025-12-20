package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.entity.DataSource;
import com.medical.research.mapper.DataSourceMapper;
import com.medical.research.dto.datasource.DataSourceReqDTO;
import com.medical.research.dto.datasource.DataSourceRespDTO;
import com.medical.research.dto.datasource.DbTestReqDTO;
import com.medical.research.service.DataSourceService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DataSourceServiceImpl extends ServiceImpl<DataSourceMapper, DataSource>
        implements DataSourceService {

    @Resource
    private DataSourceMapper dataSourceMapper;

    @Resource
    private MinioClient minioClient;

    // MinIO配置
    private final String bucketName = "medical-research";
    private final String excelPrefix = "excel/";

    @Override
    public Page<DataSourceRespDTO> getSourcePage(DataSourceReqDTO req) {
        Page<DataSourceRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = dataSourceMapper.selectSourceCount(req);
        page.setTotal(total);

        // 查询列表
        List<DataSource> sourceList = dataSourceMapper.selectSourcePage(req);
        List<DataSourceRespDTO> respList = sourceList.stream().map(source -> {
            DataSourceRespDTO resp = new DataSourceRespDTO();
            BeanUtils.copyProperties(source, resp);
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addExcelSource(String sourceName, String filePath) {
        DataSource source = new DataSource();
        source.setSourceName(sourceName);
        source.setSourceType("Excel");
        source.setFilePath(filePath);
        source.setCreateTime(LocalDateTime.now());
        source.setUpdateTime(LocalDateTime.now());
        return save(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDbSource(DataSourceReqDTO req) {
        DataSource source = new DataSource();
        BeanUtils.copyProperties(req, source);
        source.setSourceType("数据库");
        source.setCreateTime(LocalDateTime.now());
        source.setUpdateTime(LocalDateTime.now());
        return save(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDbSource(DataSourceReqDTO req) {
        DataSource source = getById(req.getId());
        if (source == null || !"数据库".equals(source.getSourceType())) {
            return false;
        }
        BeanUtils.copyProperties(req, source);
        source.setUpdateTime(LocalDateTime.now());
        return updateById(source);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSource(Long id) {
        DataSource source = getById(id);
        if (source == null) {
            return false;
        }

        // 如果是Excel数据源，删除MinIO文件
        if ("Excel".equals(source.getSourceType()) && source.getFilePath() != null) {
            try {
                minioClient.removeObject(
                        io.minio.RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(source.getFilePath())
                                .build()
                );
            } catch (Exception e) {
                log.error("删除MinIO文件失败", e);
            }
        }

        return removeById(id);
    }

    @Override
    public DataSourceRespDTO getSourceById(Long id) {
        DataSource source = getById(id);
        if (source == null) {
            return null;
        }
        DataSourceRespDTO resp = new DataSourceRespDTO();
        BeanUtils.copyProperties(source, resp);
        return resp;
    }

    @Override
    public boolean testDbConnection(DbTestReqDTO req) {
        Connection conn = null;
        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 建立连接
            conn = DriverManager.getConnection(req.getDbUrl(), req.getDbUsername(), req.getDbPassword());
            return conn.isValid(3);
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }

    @Override
    public String uploadExcelFile(MultipartFile file) {
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = excelPrefix + UUID.randomUUID().toString() + ext;

            // 上传到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;
        } catch (Exception e) {
            log.error("上传Excel文件到MinIO失败", e);
            return null;
        }
    }
}