package com.medical.research.dto.datasource;

import lombok.Data;

@Data
public class DbTestReqDTO {
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