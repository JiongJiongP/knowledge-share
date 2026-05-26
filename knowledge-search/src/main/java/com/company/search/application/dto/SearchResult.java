package com.company.search.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Data
public class SearchResult {
    private Long id;
    private String title;
    private String excerpt;
    private String contentType;
    private String createdBy;
    private LocalDateTime publishedAt;

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    };

    public static SearchResult fromSource(Long id, co.elastic.clients.elasticsearch.core.search.Hit<ContentDocument> hit) {
        SearchResult r = new SearchResult();
        r.setId(id);
        if (hit.source() != null) {
            ContentDocument src = hit.source();
            r.setTitle(src.getTitle());
            r.setContentType(src.getContentType());
            r.setCreatedBy(src.getCreatedBy());
            if (src.getPublishedAt() != null && !src.getPublishedAt().isEmpty()) {
                for (DateTimeFormatter fmt : FORMATTERS) {
                    try {
                        r.setPublishedAt(LocalDateTime.parse(src.getPublishedAt(), fmt));
                        break;
                    } catch (DateTimeParseException ignored) {}
                }
            }
            String body = src.getBody();
            if (body != null) {
                r.setExcerpt(body.replaceAll("[#*`>!\\[\\]()\\n\\r]", " ").trim().substring(0, Math.min(200, body.length())));
            }
        }
        if (hit.highlight() != null && hit.highlight().containsKey("body")) {
            String hl = String.join(" ... ", hit.highlight().get("body"));
            r.setExcerpt(hl.replaceAll("<em>", "").replaceAll("</em>", ""));
        }
        return r;
    }
}
