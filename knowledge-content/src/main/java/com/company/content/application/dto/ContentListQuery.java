package com.company.content.application.dto;

import lombok.Data;

@Data
public class ContentListQuery {
    private int page = 1;
    private int size = 10;
    private String sort = "latest";
    private String contentType;
    private String keyword;
}
