package com.github.listen_to_me.common.exception;

import cn.hutool.http.HttpStatus;
import lombok.Getter;

/**
 * 业务异常基类
 */
@Getter
public class BaseException extends RuntimeException {

    private final Integer code;

    public BaseException(String message) {
        this(HttpStatus.HTTP_INTERNAL_ERROR, message);
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
