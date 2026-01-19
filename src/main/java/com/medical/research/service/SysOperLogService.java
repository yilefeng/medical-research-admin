// 接口
package com.medical.research.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import com.medical.research.dto.sys.SysOperLogRespDTO;
import com.medical.research.entity.sys.SysOperLog;

public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    Page<SysOperLogRespDTO> getOperLogPage(SysOperLogReqDTO req);
}

