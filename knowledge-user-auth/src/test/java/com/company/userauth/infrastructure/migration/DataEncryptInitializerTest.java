package com.company.userauth.infrastructure.migration;

import com.company.common.config.Sm4Config;
import com.company.common.util.SM4Util;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataEncryptInitializerTest {

    private static final String DATA_KEY = "0123456789abcdef0123456789abcdef";

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ApplicationArguments args;
    @InjectMocks private DataEncryptInitializer initializer;

    @BeforeAll
    static void setUp() {
        Sm4Config.initializeForTest(DATA_KEY);
    }

    @Test
    void shouldEncryptUnencryptedUsers() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("id", 1L, "username", "zhangsan", "email", "zhangsan@test.com", "display_name", "张三")
        ));
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        initializer.run(args);

        verify(jdbcTemplate).update(anyString(), any(), any(), any(), any());
    }

    @Test
    void shouldSkipAlreadyEncryptedUsers() throws Exception {
        String encUsername = SM4Util.encryptDeterministic("zhangsan", DATA_KEY);
        String encEmail = SM4Util.encryptDeterministic("zhangsan@test.com", DATA_KEY);
        String encDisplayName = SM4Util.encrypt("张三", DATA_KEY);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("id", 1L, "username", encUsername, "email", encEmail, "display_name", encDisplayName)
        ));

        initializer.run(args);

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any());
    }

    @Test
    void shouldHandleEmptyUserList() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        initializer.run(args);

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any(), any());
    }
}
