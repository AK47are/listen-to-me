package com.github.listen_to_me.common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.listen_to_me.common.Result;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public Result<?> handle(BaseException e) {
        log.warn("业务异常处理 - 状态码: {}, 错误信息: {}", e.getCode(), e.getMessage());
        return Result.fail(e.getMessage());
    }
}
