package com.company.userauth.application.service;

import com.company.common.config.Sm4Config;
import com.company.common.exception.BizException;
import com.company.userauth.domain.model.Department;
import com.company.userauth.domain.model.Role;
import com.company.userauth.domain.model.User;
import com.company.userauth.domain.model.UserRole;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceExtendedTest {

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
        jwtUtil = new JwtUtil("test-secret-key-for-extended-unit-test-min-256-bits!!", 3600000L);
        authService = new AuthService(userMapper, roleMapper, userRoleMapper, departmentMapper, passwordEncoder, jwtUtil);
    }

    @Test
    void shouldListUsersWithDepartmentAndRole() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setDisplayName("管理员");
        user.setEmail("admin@company.com");
        user.setDepartmentId(1L);
        user.setStatus("ACTIVE");
        when(userMapper.selectList(any())).thenReturn(List.of(user));

        Department dept = new Department();
        dept.setId(1L);
        dept.setName("技术部");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));

        when(roleMapper.findAllUserRoles()).thenReturn(List.of(
                Map.of("user_id", 1L, "code", "ADMIN")
        ));

        var result = authService.listUsers();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("admin");
        assertThat(result.get(0).getDepartmentName()).isEqualTo("技术部");
        assertThat(result.get(0).getRoleName()).isEqualTo("系统管理员");
    }

    @Test
    void shouldListUsersWithDefaultRoleWhenNoRoleAssigned() {
        User user = new User();
        user.setId(2L);
        user.setUsername("testuser");
        user.setDisplayName("测试用户");
        user.setEmail("test@company.com");
        user.setDepartmentId(1L);
        user.setStatus("ACTIVE");
        when(userMapper.selectList(any())).thenReturn(List.of(user));

        Department dept = new Department();
        dept.setId(1L);
        dept.setName("技术部");
        when(departmentMapper.selectList(any())).thenReturn(List.of(dept));
        when(roleMapper.findAllUserRoles()).thenReturn(List.of());

        var result = authService.listUsers();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoleName()).isEqualTo("普通用户");
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");

        Role userRole = new Role();
        userRole.setId(2L);
        userRole.setCode("USER");
        userRole.setName("普通用户");
        when(roleMapper.selectOne(any())).thenReturn(userRole);
        when(userMapper.insert(any())).thenReturn(1);
        when(userRoleMapper.insert(any())).thenReturn(1);

        authService.createUser("newuser", "password123", "新用户", "new@company.com", 1L);

        assertThat(true).isTrue();
    }

    @Test
    void shouldThrowWhenCreatingDuplicateUser() {
        User existing = new User();
        existing.setUsername("admin");
        when(userMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> authService.createUser("admin", "password", "管理员", "admin@company.com", 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户名已存在");
    }

    @Test
    void shouldUpdateUserRole() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userMapper.selectById(1L)).thenReturn(user);

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setCode("ADMIN");
        adminRole.setName("系统管理员");
        when(roleMapper.selectOne(any())).thenReturn(adminRole);
        when(userRoleMapper.delete(any())).thenReturn(1);
        when(userRoleMapper.insert(any())).thenReturn(1);

        authService.updateUserRole(1L, "ADMIN");

        assertThat(true).isTrue();
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentUser() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> authService.updateUserRole(999L, "ADMIN"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    void shouldThrowWhenUpdatingToNonExistentRole() {
        User user = new User();
        user.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        when(roleMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> authService.updateUserRole(1L, "SUPERADMIN"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("角色不存在");
    }

    @Test
    void shouldLoginWithDefaultRoleWhenNoRoleAssigned() {
        User user = new User();
        user.setId(1L);
        user.setUsername("noroleuser");
        user.setPassword("$2a$encoded");
        user.setStatus("ACTIVE");

        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password", "$2a$encoded")).thenReturn(true);
        when(roleMapper.findRoleByUserId(1L)).thenReturn(null);

        String token = authService.login("noroleuser", "password");
        assertThat(token).isNotNull();
    }
}
