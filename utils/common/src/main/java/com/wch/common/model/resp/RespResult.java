package com.wch.common.model.resp;

import java.io.Serializable;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 15:23
 */
public class RespResult<T> implements Serializable {
    private T data;

    private Integer code;

    private String message;

    public RespResult() {
    }

    public RespResult(T data, Integer code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public RespResult(T data, RespCode respCode) {
        this.data = data;
        this.code = respCode.getCode();
        this.message = respCode.getMessage();
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public static RespResult success() {
        return new RespResult(null, RespCode.SUCCESS);
    }

    public static RespResult error() {
        return new RespResult(null, RespCode.ERROR);
    }

    public static RespResult fail() {
        return new RespResult(null, RespCode.BAD_REQUEST);
    }
}
