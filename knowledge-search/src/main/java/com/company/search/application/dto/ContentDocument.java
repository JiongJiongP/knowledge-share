package com.company.search.application.dto;

import lombok.Data;

@Data
public class ContentDocument {
    private String title;
    private String body;
    private String contentType;
    private String createdBy;
    private String publishedAt;
}
