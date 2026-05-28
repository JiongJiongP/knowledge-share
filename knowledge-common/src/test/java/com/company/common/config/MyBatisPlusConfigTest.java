package com.company.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisPlusConfigTest {

    private MyBatisPlusConfig config;

    @BeforeEach
    void setUp() {
        config = new MyBatisPlusConfig();
    }

    @Test
    void shouldCreateMybatisPlusInterceptor() {
        var interceptor = config.mybatisPlusInterceptor();
        assertThat(interceptor).isNotNull();
    }

    @Test
    void shouldImplementMetaObjectHandler() {
        assertThat(config).isInstanceOf(MetaObjectHandler.class);
    }

    @Test
    void shouldHaveInsertFillMethod() throws NoSuchMethodException {
        var method = MyBatisPlusConfig.class.getMethod("insertFill", org.apache.ibatis.reflection.MetaObject.class);
        assertThat(method).isNotNull();
    }

    @Test
    void shouldHaveUpdateFillMethod() throws NoSuchMethodException {
        var method = MyBatisPlusConfig.class.getMethod("updateFill", org.apache.ibatis.reflection.MetaObject.class);
        assertThat(method).isNotNull();
    }
}
