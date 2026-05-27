package com.company.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTemplateRequest {
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 128, message = "模板名称长度不能超过128个字符")
    private String name;

    @Size(max = 500, message = "模板描述长度不能超过500个字符")
    private String description;

    @Size(max = 32, message = "内容类型长度不能超过32个字符")
    private String contentType;

    @Size(max = 65535, message = "模板内容长度不能超过65535个字符")
    private String body;
}
