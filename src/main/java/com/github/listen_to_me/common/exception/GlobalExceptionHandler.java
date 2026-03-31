package com.github.listen_to_me.common.exception;

import com.github.listen_to_me.common.enumeration.UniqueIndexEnum;
import com.github.listen_to_me.common.util.ExceptionUtils;

import cn.hutool.http.HttpStatus;

import org.springframework.dao.DuplicateKeyException;
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
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 捕获唯一约束重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库唯一约束冲突", e);
        String indexName = ExceptionUtils.getDuplicateIndexName(e);
        String fieldDesc = UniqueIndexEnum.getFieldDescByIndex(indexName);
        return Result.fail(HttpStatus.HTTP_CONFLICT, fieldDesc + "已存在，请更换后重试");
    }

    // 必须放最后，否则会提前捕获
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.fail("发生未知错误");
    }

}
