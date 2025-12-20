package com.medical.research.dto.datasource;

import lombok.Data;

import java.util.Date;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:25
 * @Description:
 */
@Data
public class DataSourceRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据源类型：Excel/数据库
     */
    private String sourceType;

    /**
     * 文件路径（Excel）
     */
    private String filePath;

    /**
     * 数据库地址
     */
    private String dbUrl;

    /**
     * 数据库账号
     */
    private String dbUsername;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
