package com.company.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SensitiveWordCheckRequest {
    @NotBlank(message = "检测内容不能为空")
    @Size(max = 10000, message = "检测内容长度不能超过10000个字符")
    private String text;
}
