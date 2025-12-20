package com.medical.research.dto.sys;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:54
 * @Description:
 */
import lombok.Data;

import java.util.Date;

@Data
public class SysOperLogRespDTO {
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
     * 操作描述
     */
    private String operDesc;

    /**
     * 操作IP
     */
    private String operIp;

    /**
     * 操作时间
     */
    private Date operTime;

    /**
     * 操作结果（成功/失败）
     */
    private String operResult;

    /**
     * 错误信息
     */
    private String errorMsg;
}
