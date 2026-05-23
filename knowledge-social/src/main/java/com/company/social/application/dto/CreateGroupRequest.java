package com.company.social.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "群组名称不能为空")
    @Size(max = 128, message = "群组名称长度不能超过128个字符")
    private String name;

    @Size(max = 512, message = "群组描述长度不能超过512个字符")
    private String description;
}
