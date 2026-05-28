package com.company.search.application.dto;

import co.elastic.clients.elasticsearch.core.search.Hit;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchResultTest {

    @Test
    void shouldCreateFromSourceWithFullData() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("测试标题");
        doc.setBody("这是正文内容，包含一些**Markdown**格式");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("zhangsan");
        doc.setPublishedAt("2025-01-15T10:30:00");

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(null);

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("测试标题");
        assertThat(result.getContentType()).isEqualTo("ARTICLE");
        assertThat(result.getCreatedBy()).isEqualTo("zhangsan");
        assertThat(result.getPublishedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
        assertThat(result.getExcerpt()).isNotNull();
    }

    @Test
    void shouldHandleNullSource() {
        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(null);
        when(hit.highlight()).thenReturn(null);

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isNull();
    }

    @Test
    void shouldHandleHighlight() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");
        doc.setBody("正文内容");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("admin");
        doc.setPublishedAt(null);

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(Map.of("body", List.of("这是<em>高亮</em>内容")));

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getExcerpt()).isEqualTo("这是高亮内容");
    }

    @Test
    void shouldParseDifferentDateFormats() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");
        doc.setBody("正文");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("admin");
        doc.setPublishedAt("2025-01-15 10:30:00");

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(null);

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getPublishedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
    }

    @Test
    void shouldHandleEmptyPublishedAt() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");
        doc.setBody("正文");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("admin");
        doc.setPublishedAt("");

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(null);

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getPublishedAt()).isNull();
    }

    @Test
    void shouldHandleNullBody() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");
        doc.setBody(null);
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("admin");
        doc.setPublishedAt(null);

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(null);

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getExcerpt()).isNull();
    }

    @Test
    void shouldHandleMultipleHighlightFragments() {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("标题");
        doc.setBody("正文");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("admin");
        doc.setPublishedAt(null);

        @SuppressWarnings("unchecked")
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(Map.of("body", List.of("片段1<em>高亮</em>", "片段2<em>高亮</em>")));

        SearchResult result = SearchResult.fromSource(1L, hit);

        assertThat(result.getExcerpt()).contains("片段1高亮");
        assertThat(result.getExcerpt()).contains("片段2高亮");
    }
}
