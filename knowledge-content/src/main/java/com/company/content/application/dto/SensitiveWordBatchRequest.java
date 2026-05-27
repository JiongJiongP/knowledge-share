package com.company.content.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class SensitiveWordBatchRequest {
    @NotEmpty(message = "敏感词列表不能为空")
    private List<@Size(max = 64) String> words;

    @Size(max = 32)
    private String category;
}
