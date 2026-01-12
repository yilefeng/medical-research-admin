package com.medical.research.entity.experiment;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Auther: yilefeng
 * @Date: 2026/1/12 14:57
 * @Description:
 */
@Accessors(chain = true)
@Data
@TableName("experiment_researcher")
public class ExperimentResearcher {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联实验ID
     */
    @TableField("experiment_id")
    private Long experimentId;

    /**
     * 研究员ID——关联用户ID
     */
    @TableField("researcher_id")
    private Long researcherId;

    /**
     * 状态（1：正常，0：删除）
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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