package com.company.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTagRequest {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 64, message = "标签名称长度不能超过64个字符")
    private String name;

    @Size(max = 7, message = "颜色格式不正确")
    private String color;
}
