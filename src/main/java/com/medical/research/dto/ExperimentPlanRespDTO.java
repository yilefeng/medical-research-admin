package com.medical.research.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class ExperimentPlanRespDTO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 实验方案名称
     */
    private String planName;

    /**
     * 实验编号
     */
    private String experimentNo;

    /**
     * 实验目的
     */
    private String purpose;

    /**
     * 负责人
     */
    private String principal;

    /**
     * 所属科室
     */
    private String dept;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态：0-未开始 1-进行中 2-已完成 3-已终止
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}