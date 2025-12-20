package com.medical.research.dto.stat;
import lombok.Data;

/**
 * 统计模型请求DTO
 */
@Data
public class StatModelReqDTO {
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
}
