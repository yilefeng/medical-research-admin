package com.medical.research.dto.stat;

import lombok.Data;

import java.util.Date;

@Data
public class StatModelRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型编码（如t_test/chi_square）
     */
    private String modelCode;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}