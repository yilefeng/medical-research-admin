// 实现类
package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import com.medical.research.dto.sys.SysOperLogRespDTO;
import com.medical.research.entity.sys.SysOperLog;
import com.medical.research.mapper.SysOperLogMapper;
import com.medical.research.service.SysOperLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: yilefeng
 * @Date: 2025/12/17 15:54
 * @Description:
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
        implements SysOperLogService {

    @Resource
    private SysOperLogMapper sysOperLogMapper;


    @Override
    public Page<SysOperLogRespDTO> getOperLogPage(SysOperLogReqDTO req) {
        Page<SysOperLogRespDTO> page = new Page<>(req.getPageNum(), req.getPageSize());

        // 查询总数
        Long total = sysOperLogMapper.selectOperLogCount(req);
        page.setTotal(total);

        // 查询列表
        List<SysOperLog> logList = sysOperLogMapper.selectOperLogPage(req);
        List<SysOperLogRespDTO> respList = logList.stream().map(log -> {
            SysOperLogRespDTO resp = new SysOperLogRespDTO();
            BeanUtils.copyProperties(log, resp);
            return resp;
        }).collect(Collectors.toList());

        page.setRecords(respList);
        return page;
    }
}
