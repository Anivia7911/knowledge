package com.wch.common.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @author: Jie Bugui
 * @create: 2025-04-22 15:23
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RespResult<T> implements Serializable {

    private Integer code;

    private String message;

    private T data;

    public RespResult(T data, RespCode respCode) {
        this.data = data;
        this.code = respCode.getCode();
        this.message = respCode.getMessage();
    }

    public static <T> RespResult<T> success() {
        return new RespResult<>(null, RespCode.SUCCESS);
    }

    public static <T> RespResult<T> success(T data) {
        return new RespResult<>(data, RespCode.SUCCESS);
    }

    public static <T> RespResult<T> error() {
        return new RespResult<>(null, RespCode.ERROR);
    }

    public static <T> RespResult<T> fail() {
        return new RespResult<>(null, RespCode.BAD_REQUEST);
    }
}
