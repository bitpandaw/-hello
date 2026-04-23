package com.mall.common.web;

import com.mall.common.api.Result;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handle(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result<?> valid(Exception e) {
        String m = e.getMessage();
        if (e instanceof MethodArgumentNotValidException v && v.getBindingResult().getFieldError() != null) {
            m = v.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException b && b.getBindingResult().getFieldError() != null) {
            m = b.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), m);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> auth(AuthenticationException e) {
        return Result.error(ResultCode.UNAUTHORIZED.getCode(), e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> denied(AccessDeniedException e) {
        return Result.error(ResultCode.FORBIDDEN.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> other(Exception e) {
        log.error("unhandled", e);
        return Result.error(ResultCode.FAILED.getCode(), "系统繁忙");
    }
}
