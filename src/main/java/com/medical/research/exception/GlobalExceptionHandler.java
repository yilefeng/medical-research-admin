package com.medical.research.exception;

import com.medical.research.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器：统一返回异常结果
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {}，请求URI: {}", e.getMessage(), request.getRequestURI());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public Result<?> handleValidException(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof BindException) {
            msg = ((BindException) e).getFieldError().getDefaultMessage();
        } else if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getFieldError().getDefaultMessage();
        }
        log.error("参数校验异常: {}", msg);
        return Result.error(400, msg);
    }

    /**
     * 权限不足异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足: {}", e.getMessage());
        return Result.error(403, "权限不足，无法执行该操作");
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<?> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("接口不存在: {}", e.getMessage());
        return Result.error(404, "请求的接口不存在");
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}，请求URI: {}", e.getMessage(), request.getRequestURI(), e);
        return Result.error(500, "系统内部异常，请联系管理员");
    }
}