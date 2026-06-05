package com.miji.handler;

import com.common.enums.CodeEnum;
import com.common.exception.CustomException;
import com.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result handleCustomException(CustomException e) {
        return Result.fail(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? CodeEnum.PARAM_ERROR.getMsg()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.fail(CodeEnum.PARAM_ERROR.getStatusCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? CodeEnum.PARAM_ERROR.getMsg()
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.fail(CodeEnum.PARAM_ERROR.getStatusCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("system error", e);
        return Result.fail(CodeEnum.COMMON_ERROR.getStatusCode(), "system error");
    }
}
