package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志表实体类
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

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