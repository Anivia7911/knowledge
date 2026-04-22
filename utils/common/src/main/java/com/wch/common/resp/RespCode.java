package com.wch.common.resp;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 15:14
 */
public enum RespCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求错误"),
    ERROR(500, "服务器错误"),

    ;

    private Integer code;
    private String message;

    RespCode(Integer code, String message) {
        this.code = code;
        this.message = message;

    }

    RespCode() {
    }

    public Integer getCode(){
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
