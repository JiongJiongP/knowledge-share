package com.company.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BizExceptionTest {

    @Test
    void shouldCreateWithCodeAndMessage() {
        BizException e = new BizException(400, "参数错误");
        assertThat(e.getCode()).isEqualTo(400);
        assertThat(e.getMessage()).isEqualTo("参数错误");
    }

    @Test
    void shouldDefaultTo500WhenCodeNotSpecified() {
        BizException e = new BizException("服务器错误");
        assertThat(e.getCode()).isEqualTo(500);
        assertThat(e.getMessage()).isEqualTo("服务器错误");
    }

    @Test
    void shouldCreateNotFoundException() {
        BizException e = BizException.notFound("内容");
        assertThat(e.getCode()).isEqualTo(404);
        assertThat(e.getMessage()).isEqualTo("内容不存在");
    }

    @Test
    void shouldCreateForbiddenException() {
        BizException e = BizException.forbidden();
        assertThat(e.getCode()).isEqualTo(403);
        assertThat(e.getMessage()).isEqualTo("权限不足");
    }

    @Test
    void shouldCreateBadRequestException() {
        BizException e = BizException.badRequest("参数不合法");
        assertThat(e.getCode()).isEqualTo(400);
        assertThat(e.getMessage()).isEqualTo("参数不合法");
    }

    @Test
    void shouldBeRuntimeException() {
        BizException e = new BizException(500, "test");
        assertThat(e).isInstanceOf(RuntimeException.class);
    }
}
