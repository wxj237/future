package io.renren.common.utils;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 */
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String msg;
    private T data;
    
    public ApiResponse() {
    }
    
    public ApiResponse(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success() {
        return success("操作成功", null);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return success("操作成功", data);
    }
    
    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(0, msg, data);
    }
    
    public static <T> ApiResponse<T> error() {
        return error(500, "操作失败");
    }
    
    public static <T> ApiResponse<T> error(String msg) {
        return error(500, msg);
    }
    
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
    
    // 检查token是否即将过期 (距离过期时间小于30分钟)
    public static boolean isAboutToExpire(Long expireTime) {
        if (expireTime == null) {
            return true;
        }
        // 30分钟 = 30 * 60 * 1000 毫秒
        return (expireTime - System.currentTimeMillis()) < (30 * 60 * 1000);
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return code == 0;
    }
}