package io.renren.common.exception;

import io.renren.common.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception e) {
        logger.error("系统异常:", e);
        return ApiResponse.error(500, "系统内部错误，请联系管理员");
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(RenException.class)
    public ApiResponse handleRenException(RenException e) {
        logger.error("自定义异常:", e);
        return ApiResponse.error(e.getCode(), e.getMsg());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("参数校验异常:", e);
        return ApiResponse.error(400, "参数错误: " + e.getMessage());
    }
}