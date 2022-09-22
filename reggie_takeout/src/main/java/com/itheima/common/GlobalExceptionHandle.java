package com.itheima.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandle {
    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> ExceptionHandler(SQLIntegrityConstraintViolationException exception){
        //日志记录出错信息
        log.error(exception.getMessage());
        if(exception.getMessage().contains("Duplicate entry")){
            String[] split = exception.getMessage().split(" ");//将exception.getMessage中的信息以分割号为标记,分割为split数组
            String message = split[2] + "已存在";
            return Result.error(message);
        }
        return Result.error("未知异常");
    }
    @ExceptionHandler(CustomException.class)
    public Result<String> ExceptionHandler(CustomException exception){
        log.error(exception.getMessage());
        return Result.error(exception.getMessage());
    }


}
