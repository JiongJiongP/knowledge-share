package com.company.social.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveMemberRequest {
    @NotBlank(message = "操作不能为空")
    private String action;
}
