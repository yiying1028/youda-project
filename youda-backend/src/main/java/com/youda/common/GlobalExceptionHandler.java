package com.youda.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数错误";
        return Result.error(400, message);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<?> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        if ("file".equals(e.getRequestPartName())) {
            return Result.error(400, "未检测到上传文件，请重新选择文件");
        }
        return Result.error(400, "缺少必要的上传参数: " + e.getRequestPartName());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(400, "上传文件不能超过50MB");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<?> handleMultipartException(MultipartException e) {
        String message = e.getMessage();
        if (message != null && message.toLowerCase().contains("boundary")) {
            return Result.error(400, "上传请求格式错误，请勿手动设置 Content-Type");
        }
        return Result.error(400, "上传请求解析失败，请重新上传");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误");
    }
}
