package com.medical.research.aspect;

import com.alibaba.fastjson2.JSON;
import com.medical.research.entity.sys.SysOperLog;
import com.medical.research.service.SysOperLogService;
import com.medical.research.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {
    private final SysOperLogService operLogService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 定义切点：所有controller包下的方法
    @Pointcut("execution(* com.medical.research.controller..*(..))")
    public void operLogPointcut() {}

    // 后置通知：记录日志
    @AfterReturning(pointcut = "operLogPointcut()", returning = "result")
    public void saveOperLog(JoinPoint joinPoint, Object result) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();

            // 获取token解析用户信息
            String token = request.getHeader("token");
            if (token == null || token.isEmpty()) return;

            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Long userId = Long.parseLong(userDetails.getUsername().split("_")[0]); // 实际项目需从token解析用户ID

            // 构建日志对象
            SysOperLog operLog = new SysOperLog();
            operLog.setUserId(userId);
            operLog.setUsername(username);
            operLog.setOperIp(request.getRemoteAddr());
            operLog.setOperTime(LocalDateTime.now());

            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            operLog.setOperModule(method.getDeclaringClass().getSimpleName()); // 模块名
            operLog.setOperType(getOperType(method.getName())); // 操作类型
            if (result != null) {
                operLog.setOperContent("请求参数：" + JSON.toJSONString(joinPoint.getArgs()) + " | 返回结果：" + JSON.toJSONString(result));
            }
            // 保存日志
            operLogService.save(operLog);
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }
    }

    // 根据方法名判断操作类型
    private String getOperType(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "新增";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "修改";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "删除";
        } else if (methodName.startsWith("import")) {
            return "导入";
        } else if (methodName.startsWith("export")) {
            return "导出";
        } else {
            return "查询";
        }
    }
}