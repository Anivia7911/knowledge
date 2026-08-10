package com.wch.common.handler;

import com.wch.common.model.resp.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器，将异常转换为统一的响应结构，避免直接返回 500 页面
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 文件上传大小超限
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RespResult<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限: {}", e.getMessage());
        return RespResult.error("上传文件大小超出限制");
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    public RespResult<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务处理异常: {}", e.getMessage(), e);
        return RespResult.error(e.getMessage());
    }

    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public RespResult<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return RespResult.error("系统异常，请稍后重试");
    }
}
