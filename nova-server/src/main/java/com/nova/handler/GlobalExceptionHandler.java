package com.nova.handler;

import com.nova.constant.MessageConstant;
import com.nova.exception.BaseException;
import com.nova.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex) {
        log.error("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result sqlHandler(SQLIntegrityConstraintViolationException ex) {
        String msg = ex.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            String[] split = msg.split(" ");
            return Result.error(split[2] + MessageConstant.ALREADY_EXISTS);
        }
        return Result.error("数据库操作失败");
    }

    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception ex) {
        log.error("未知异常", ex);
        return Result.error("服务器繁忙，请稍后重试");
    }
}
