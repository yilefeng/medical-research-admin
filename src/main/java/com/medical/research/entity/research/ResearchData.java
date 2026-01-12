package com.medical.research.entity.research;

import com.baomidou.mybatisplus.annotation.*;
import com.medical.research.entity.BaseDO;
import lombok.Data;
import lombok.Getter;

/**
 * 科研数据表实体类
 */
@Data
@TableName("research_data")
public class ResearchData extends BaseDO {

    private Long experimentId;

    private Integer trueLabel;

    private Double model1Score;

    private Double model2Score;

    private String dataSource;

    //状态（1：正常，0：删除）
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