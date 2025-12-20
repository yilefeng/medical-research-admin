package com.medical.research.util;

import lombok.Data;

/**
 * 统一返回结果类
 */
@Data
public class Result<T> {
    /** 响应码：200成功，其他失败 */
    private int code;
    /** 响应消息 */
    private String msg;
    /** 响应数据 */
    private T data;

    // 成功返回
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 失败返回
    public static <T> Result<T> error() {
        return error("操作失败");
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}