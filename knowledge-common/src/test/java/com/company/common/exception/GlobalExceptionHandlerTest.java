package com.company.common.exception;

import com.company.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBizException() {
        BizException e = new BizException(404, "资源不存在");
        ResponseEntity<Result<?>> response = handler.handleBiz(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("资源不存在");
    }

    @Test
    void shouldHandleBizExceptionWithCustomCode() {
        BizException e = new BizException(401, "未授权");
        ResponseEntity<Result<?>> response = handler.handleBiz(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getCode()).isEqualTo(401);
    }

    @Test
    void shouldHandleValidationException() {
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(e.getBindingResult()).thenReturn(bindingResult);
        FieldError fieldError = new FieldError("obj", "name", "不能为空");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Result<?>> response = handler.handleValidation(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("name");
        assertThat(response.getBody().getMessage()).contains("不能为空");
    }

    @Test
    void shouldHandleHttpMessageNotReadable() {
        HttpMessageNotReadableException e = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Result<?>> response = handler.handleBadRequest(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("请求格式错误");
    }

    @Test
    void shouldHandleMethodNotSupported() {
        HttpRequestMethodNotSupportedException e = mock(HttpRequestMethodNotSupportedException.class);

        ResponseEntity<Result<?>> response = handler.handleMethodNotSupported(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getCode()).isEqualTo(405);
        assertThat(response.getBody().getMessage()).isEqualTo("不支持的请求方法");
    }

    @Test
    void shouldHandleMissingParam() {
        MissingServletRequestParameterException e = new MissingServletRequestParameterException("page", "int");

        ResponseEntity<Result<?>> response = handler.handleMissingParam(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("page");
    }

    @Test
    void shouldHandleUnknownException() {
        Exception e = new RuntimeException("unexpected");

        ResponseEntity<Result<?>> response = handler.handleUnknown(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("服务器内部错误");
    }

    @Test
    void shouldHandleMultipleValidationErrors() {
        MethodArgumentNotValidException e = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(e.getBindingResult()).thenReturn(bindingResult);
        FieldError fe1 = new FieldError("obj", "name", "不能为空");
        FieldError fe2 = new FieldError("obj", "email", "格式不正确");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fe1, fe2));

        ResponseEntity<Result<?>> response = handler.handleValidation(e);

        assertThat(response.getBody().getMessage()).contains("name");
        assertThat(response.getBody().getMessage()).contains("email");
    }
}
