package com.medical.research.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/25 15:16
 * @Description:
 */
@Data
@Accessors(chain = true)
public class BaseDO {
    /**
     * 主键 id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
