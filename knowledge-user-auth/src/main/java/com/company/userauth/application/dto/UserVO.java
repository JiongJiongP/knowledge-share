package com.company.userauth.application.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private String roleName;
    private String status;
    private String createdAt;
}
