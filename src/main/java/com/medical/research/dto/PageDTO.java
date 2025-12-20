package com.medical.research.dto;

import lombok.Data;

/**
 * 分页请求通用DTO
 */
@Data
public class PageDTO {
    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;

    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}