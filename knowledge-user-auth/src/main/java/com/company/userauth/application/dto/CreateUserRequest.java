package com.company.userauth.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 128, message = "密码长度6-128位")
    private String password;

    @Size(max = 64)
    private String displayName;

    @Size(max = 128)
    private String email;

    private Long departmentId;
}
