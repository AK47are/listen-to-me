package com.github.listen_to_me.common.exception;

import cn.hutool.http.HttpStatus;

/**
 * 资源冲突异常（409）
 */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(HttpStatus.HTTP_CONFLICT, message);
    }
}
