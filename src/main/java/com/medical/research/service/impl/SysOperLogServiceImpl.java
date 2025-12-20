// 实现类
package com.medical.research.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.research.entity.SysOperLog;
import com.medical.research.mapper.SysOperLogMapper;
import com.medical.research.dto.sys.SysOperLogReqDTO;
import com.medical.research.dto.sys.SysOperLogRespDTO;
import com.medical.research.service.SysOperLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
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

    // 获取客户端IP
    private static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLog(String username, String operModule, String operType,
                          String operDesc, String operResult, String errorMsg,
                          HttpServletRequest request) {
        SysOperLog log = new SysOperLog();
        log.setUsername(StringUtils.hasText(username) ? username : "匿名用户");
        log.setOperModule(operModule);
        log.setOperType(operType);
        log.setOperIp(getIpAddress(request));
        log.setOperTime(LocalDateTime.now());
        log.setOperContent(operDesc);
        save(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanLog() {
        // 清理3个月前的日志
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date threeMonthsAgo = new Date(System.currentTimeMillis() - 3L * 30 * 24 * 60 * 60 * 1000);
        String beforeTime = sdf.format(threeMonthsAgo);

        int count = sysOperLogMapper.cleanLog(beforeTime);
        return count > 0;
    }
}
