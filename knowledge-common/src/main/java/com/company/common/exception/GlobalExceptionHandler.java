package com.company.common.exception;

import com.company.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<?>> handleBiz(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.status(400)
                .body(Result.fail(400, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleBadRequest(Exception e) {
        log.warn("请求格式错误: {}", e.getMessage());
        return ResponseEntity.status(400)
                .body(Result.fail(400, "请求格式错误"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(405)
                .body(Result.fail(405, "不支持的请求方法"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<?>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(400)
                .body(Result.fail(400, "缺少必要参数: " + e.getParameterName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return ResponseEntity.status(500)
                .body(Result.fail(500, "服务器内部错误"));
    }
}
