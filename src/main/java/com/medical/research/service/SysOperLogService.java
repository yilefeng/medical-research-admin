// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.entity.SysOperLog;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import com.medical.research.dto.sys.SysOperLogRespDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;

public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    Page<SysOperLogRespDTO> getOperLogPage(SysOperLogReqDTO req);

    /**
     * 记录操作日志
     */
    void recordLog(String username, String operModule, String operType,
                   String operDesc, String operResult, String errorMsg,
                   HttpServletRequest request);

    /**
     * 清理日志（保留近3个月）
     */
    boolean cleanLog();
}

