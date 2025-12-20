package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源表实体类
 */
@Data
@TableName("data_source")
public class DataSource {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 数据源名称 */
    private String sourceName;

    /** 数据源类型：Excel/数据库/接口 */
    private String sourceType;

    /** 文件路径（Excel） */
    private String filePath;

    /** 数据库连接地址 */
    private String dbUrl;

    /** 数据库用户名 */
    private String dbUsername;

    /** 数据库密码 */
    private String dbPassword;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}