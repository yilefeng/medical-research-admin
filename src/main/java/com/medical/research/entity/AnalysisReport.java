package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分析报告表实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报告名称 */
//    @TableField("report_name")
    private String reportName;

    /** 关联实验方案ID */
//    @TableField("plan_id")
    private Long planId;

    /** 关联统计模型ID */
//    @TableField("model_id")
    private Long modelId;

    /** 统计筛选条件（JSON） */
//    @TableField("stat_conditions")
    private String statConditions;

    /** 报告内容（HTML/Markdown） */
//    @TableField("report_content")
    private String reportContent;

    /** 报告文件存储路径 */
//    @TableField("file_url")
    private String fileUrl;

    /** 报告版本 */
    private String version;

    /** 状态：1-已生成，2-已导出，3-已作废 */
    private Integer status;

    /** 创建人ID */
    private Long userId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}