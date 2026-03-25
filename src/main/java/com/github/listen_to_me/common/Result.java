package com.github.listen_to_me.common;

import cn.hutool.http.HttpStatus;
import lombok.Data;
import java.io.Serializable;

/**
 * 用于统一封装返回给前端的结果
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return build(HttpStatus.HTTP_OK, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return build(HttpStatus.HTTP_OK, "操作成功", data);
    }

    public static <T> Result<T> fail(String msg) {
        return build(HttpStatus.HTTP_INTERNAL_ERROR, msg, null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    private static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
