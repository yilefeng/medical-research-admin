package com.medical.research.dto.experiment;

import com.medical.research.dto.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExperimentPlanReqDTO extends PageDTO {

    /**
     * 实验方案ID
     */
    private Long id;

    /**
     * 实验方案名称
     */
    private String planName;

    /**
     * 研究目的
     */
    private String researchPurpose;

    /**
     * 模型信息
     */
    private String modelInfo;

    /**
     * 实验描述
     */
    private String experimentDesc;

    /**
     * 创建人ID
     */
    private Long ownerId;

    /**
     * 是否公开（0：不公开，1：公开）
     */
    private Boolean isPublic;

    /**
     * 关联的科研ID列表
     */
    private List<Long> researchIds;

    /**
     * 状态（1：正常，0：删除）
     */
    private Integer status;
}