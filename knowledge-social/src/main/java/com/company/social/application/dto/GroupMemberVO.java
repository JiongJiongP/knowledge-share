package com.company.social.application.dto;

import lombok.Data;

@Data
public class GroupMemberVO {
    private Long id;
    private Long groupId;
    private Long userId;
    private String userName;
    private String displayName;
    private String role;
    private String status;
    private String joinedAt;
    private String createdAt;
}
