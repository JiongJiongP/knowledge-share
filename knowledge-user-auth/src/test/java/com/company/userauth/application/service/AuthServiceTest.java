package com.company.userauth.application.service;

import com.company.common.config.Sm4Config;
import com.company.common.exception.BizException;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.DepartmentMapper;
import com.company.userauth.infrastructure.mapper.RoleMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.mapper.UserRoleMapper;
import com.company.userauth.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeAll
    static void initSm4() {
        Sm4Config.initializeForTest("0123456789abcdef0123456789abcdef");
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-for-unit-test-min-256-bits!!", 3600000L);
        authService = new AuthService(userMapper, roleMapper, userRoleMapper, departmentMapper, passwordEncoder, jwtUtil);
    }

    @Test
    void shouldReturnTokenOnValidLogin() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$encoded");
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("admin123", "$2a$encoded")).thenReturn(true);
        when(roleMapper.findRoleByUserId(1L)).thenReturn("ADMIN");

        String token = authService.login("admin", "admin123");

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.login("nobody", "password"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void shouldThrowWhenPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$encoded");
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("wrong", "$2a$encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void shouldThrowWhenAccountDisabled() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("$2a$encoded");
        user.setStatus("DISABLED");

        when(userMapper.selectOne(any())).thenReturn(user);

        assertThatThrownBy(() -> authService.login("admin", "admin123"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("账号已被禁用");
    }

    @Test
    void shouldReturnUserWithoutPasswordForGetCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("secret");

        when(userMapper.selectById(1L)).thenReturn(user);

        User result = authService.getCurrentUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getPassword()).isNull();
    }

    @Test
    void shouldThrowForGetCurrentUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> authService.getCurrentUser(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
    }
}
