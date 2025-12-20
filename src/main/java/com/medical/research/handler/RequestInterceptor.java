package com.medical.research.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 接口统一拦截器：记录请求信息、权限预检、参数校验
 */
@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    /**
     * 请求处理前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 记录请求基本信息
        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        log.info("===== 请求开始 =====");
        log.info("请求URI: {}", requestUri);
        log.info("请求方法: {}", method);
        log.info("客户端IP: {}", ip);

        // 2. 打印请求头
        log.info("请求头信息:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("{}: {}", headerName, request.getHeader(headerName));
        }

        // 3. 放行标记（可添加权限预检逻辑）
        return true;
    }

    /**
     * 请求处理后（视图渲染前）
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("===== 请求处理完成 =====");
    }

    /**
     * 请求完成后（视图渲染后）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            log.error("请求处理异常: {}", ex.getMessage(), ex);
        }
        log.info("===== 请求结束 =====\n");
    }
}