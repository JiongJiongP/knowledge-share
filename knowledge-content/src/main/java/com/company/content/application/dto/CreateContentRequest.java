package com.company.content.application.dto;

import com.company.content.domain.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateContentRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 256, message = "标题长度不能超过256个字符")
    private String title;

    private String body;

    @NotNull(message = "内容类型不能为空")
    private ContentType contentType;
}
