package com.medical.research.dto.sys;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:54
 * @Description:
 */
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class SysOperLogRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /** 操作用户名 */
    private String username;

    /** 操作模块 */
    private String operModule;

    /** 操作类型：新增/修改/删除/查询/导入/导出 */
    private String operType;

    /** 操作内容 */
    private String operContent;

    /** 操作IP */
    private String operIp;

    /** 操作时间 */
    private LocalDateTime operTime;
}
