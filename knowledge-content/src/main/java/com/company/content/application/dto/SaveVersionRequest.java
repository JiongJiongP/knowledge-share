package com.company.content.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaveVersionRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @Size(max = 65535, message = "内容长度不能超过65535个字符")
    private String body;

    @Size(max = 500, message = "变更摘要长度不能超过500个字符")
    private String changeSummary;
}
