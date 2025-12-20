package com.medical.research.dto.datasource;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors
@EqualsAndHashCode(callSuper = true)
public class DataSourceReqDTO extends PageDTO {
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
     * 数据库密码
     */
    private String dbPassword;
}
