package com.company.search.application.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentDocumentTest {

    @Test
    void shouldSetAndGetAllFields() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("测试标题");
        doc.setBody("测试正文");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("zhangsan");
        doc.setPublishedAt("2025-01-15T10:30:00");

        assertThat(doc.getTitle()).isEqualTo("测试标题");
        assertThat(doc.getBody()).isEqualTo("测试正文");
        assertThat(doc.getContentType()).isEqualTo("ARTICLE");
        assertThat(doc.getCreatedBy()).isEqualTo("zhangsan");
        assertThat(doc.getPublishedAt()).isEqualTo("2025-01-15T10:30:00");
    }

    @Test
    void shouldHandleNullFields() {
        ContentDocument doc = new ContentDocument();

        assertThat(doc.getTitle()).isNull();
        assertThat(doc.getBody()).isNull();
        assertThat(doc.getContentType()).isNull();
        assertThat(doc.getCreatedBy()).isNull();
        assertThat(doc.getPublishedAt()).isNull();
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        ContentDocument doc1 = new ContentDocument();
        doc1.setTitle("标题");
        doc1.setBody("正文");

        ContentDocument doc2 = new ContentDocument();
        doc2.setTitle("标题");
        doc2.setBody("正文");

        assertThat(doc1).isEqualTo(doc2);
        assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
    }

    @Test
    void shouldTestToString() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");

        String str = doc.toString();
        assertThat(str).contains("标题");
    }
}
