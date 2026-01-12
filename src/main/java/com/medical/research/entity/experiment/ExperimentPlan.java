package com.medical.research.entity.experiment;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.research.entity.BaseDO;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 实验方案表实体类
 */
@Data
@TableName("experiment_plan")
public class ExperimentPlan extends BaseDO {

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
     * 负责人 ID
     */
    private Long ownerId;

    /**
     * 是否公开（0：不公开，1：公开）
     */
    private Boolean isPublic;

    /**
     * 状态（1：正常，0：删除）
     */
    private Integer status;

    @Getter
    public static enum Status {
        NORMAL(1),
        DELETED(0);
        private final Integer value;
        Status(Integer value) {
            this.value = value;
        }
    }
}