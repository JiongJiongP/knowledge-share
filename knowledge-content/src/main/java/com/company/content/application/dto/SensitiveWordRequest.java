package com.company.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SensitiveWordRequest {
    @NotBlank(message = "敏感词不能为空")
    @Size(max = 64, message = "敏感词长度不能超过64个字符")
    private String word;

    @Size(max = 32, message = "分类长度不能超过32个字符")
    private String category;
}
