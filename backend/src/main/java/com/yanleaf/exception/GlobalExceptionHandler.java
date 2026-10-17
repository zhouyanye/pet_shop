package com.yanleaf.exception;

import com.yanleaf.utils.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 未登录 / token 无效 → HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorized(UnauthorizedException e) {
        return Result.fail(401, e.getMessage());
    }

    /**
     * 其他运行时异常 → HTTP 200 + success=false（保持你原来的风格）
     * 如果你希望 500，可以改成 @ResponseStatus(INTERNAL_SERVER_ERROR)
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        return Result.fail(e.getMessage());
    }
}