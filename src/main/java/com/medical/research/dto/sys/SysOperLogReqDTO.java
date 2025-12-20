package com.medical.research.dto.sys;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 操作日志请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperLogReqDTO extends PageDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作模块
     */
    private String operModule;

    /**
     * 操作类型（新增/修改/删除/查询等）
     */
    private String operType;

    /**
     * 操作结果（成功/失败）
     */
    private String operResult;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
