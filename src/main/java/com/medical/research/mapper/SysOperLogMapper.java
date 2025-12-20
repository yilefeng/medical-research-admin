package com.medical.research.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medical.research.entity.SysOperLog;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    List<SysOperLog> selectOperLogPage(@Param("req") SysOperLogReqDTO req);

    /**
     * 查询操作日志总数
     */
    Long selectOperLogCount(@Param("req") SysOperLogReqDTO req);

    /**
     * 清理指定时间前的日志
     */
    int cleanLog(@Param("beforeTime") String beforeTime);
}