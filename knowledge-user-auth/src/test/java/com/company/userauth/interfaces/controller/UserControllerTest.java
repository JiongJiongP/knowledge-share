package com.company.userauth.interfaces.controller;

import com.company.common.exception.BizException;
import com.company.common.exception.GlobalExceptionHandler;
import com.company.common.result.Result;
import com.company.userauth.application.dto.UserVO;
import com.company.userauth.application.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListUsers() throws Exception {
        UserVO vo = new UserVO();
        vo.setId(1L);
        vo.setUsername("admin");
        vo.setDisplayName("管理员");
        vo.setRoleName("系统管理员");
        when(authService.listUsers()).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].username").value("admin"));
    }

    @Test
    void shouldCreateUser() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "newuser",
                "password", "password123",
                "displayName", "新用户",
                "email", "new@company.com",
                "departmentId", 1
        );
        doNothing().when(authService).createUser("newuser", "password123", "新用户", "new@company.com", 1L);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldUpdateUserRole() throws Exception {
        Map<String, String> body = Map.of("role", "ADMIN");
        doNothing().when(authService).updateUserRole(1L, "ADMIN");

        mockMvc.perform(put("/api/admin/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn400WhenCreatingDuplicateUser() throws Exception {
        Map<String, Object> body = Map.of(
                "username", "admin",
                "password", "password123",
                "displayName", "管理员",
                "email", "admin@company.com",
                "departmentId", 1
        );
        doThrow(new BizException(400, "用户名已存在"))
                .when(authService).createUser("admin", "password123", "管理员", "admin@company.com", 1L);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
