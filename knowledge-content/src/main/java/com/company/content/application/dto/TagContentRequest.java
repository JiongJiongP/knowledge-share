package com.company.content.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class TagContentRequest {
    private List<Long> tagIds;
}
