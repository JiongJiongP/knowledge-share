package com.company.search.application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import com.company.search.application.dto.ContentDocument;
import com.company.search.application.dto.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceExtendedTest {

    @Mock private ElasticsearchClient esClient;
    @Mock private VectorSearchService vectorSearchService;
    @InjectMocks private SearchService searchService;

    @Test
    void shouldIndexContent() throws Exception {
        IndexResponse response = mock(IndexResponse.class);
        when(esClient.index(any(IndexRequest.class))).thenReturn(response);

        searchService.indexContent(1L, "标题", "正文", "ARTICLE", "zhangsan", "2025-01-15");

        verify(esClient).index(any(IndexRequest.class));
    }

    @Test
    void shouldHandleIndexContentIOException() throws Exception {
        when(esClient.index(any(IndexRequest.class))).thenThrow(new IOException("Connection refused"));

        searchService.indexContent(1L, "标题", "正文", "ARTICLE", "zhangsan", "2025-01-15");

        verify(esClient).index(any(IndexRequest.class));
    }

    @Test
    void shouldDeleteContent() throws Exception {
        DeleteResponse response = mock(DeleteResponse.class);
        when(esClient.delete(any(DeleteRequest.class))).thenReturn(response);

        searchService.deleteContent(1L);

        verify(esClient).delete(any(DeleteRequest.class));
    }

    @Test
    void shouldHandleDeleteContentIOException() throws Exception {
        when(esClient.delete(any(DeleteRequest.class))).thenThrow(new IOException("Connection refused"));

        searchService.deleteContent(1L);

        verify(esClient).delete(any(DeleteRequest.class));
    }

    @Test
    void shouldBatchIndex() throws Exception {
        BulkResponse response = mock(BulkResponse.class);
        when(esClient.bulk(any(BulkRequest.class))).thenReturn(response);

        searchService.batchIndex(
                List.of(1L, 2L),
                List.of("标题1", "标题2"),
                List.of("正文1", "正文2"),
                List.of("ARTICLE", "TUTORIAL"),
                List.of("user1", "user2"),
                List.of("2025-01-15", "2025-01-16")
        );

        verify(esClient).bulk(any(BulkRequest.class));
    }

    @Test
    void shouldHandleBatchIndexIOException() throws Exception {
        when(esClient.bulk(any(BulkRequest.class))).thenThrow(new IOException("Connection refused"));

        searchService.batchIndex(
                List.of(1L),
                List.of("标题1"),
                List.of("正文1"),
                List.of("ARTICLE"),
                List.of("user1"),
                List.of("2025-01-15")
        );

        verify(esClient).bulk(any(BulkRequest.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSearchWithResults() throws Exception {
        ContentDocument doc = new ContentDocument();
        doc.setTitle("测试标题");
        doc.setBody("测试正文");
        doc.setContentType("ARTICLE");
        doc.setCreatedBy("zhangsan");
        doc.setPublishedAt("2025-01-15T10:00:00");

        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");
        when(hit.source()).thenReturn(doc);
        when(hit.highlight()).thenReturn(null);

        HitsMetadata<ContentDocument> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));

        SearchResponse<ContentDocument> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(esClient.search(any(SearchRequest.class), eq(ContentDocument.class)))
                .thenReturn(searchResponse);

        List<SearchResult> results = searchService.search("测试", 1, 10, "relevance");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("测试标题");
    }

    @Test
    void shouldReturnEmptyOnSearchIOException() throws Exception {
        when(esClient.search(any(SearchRequest.class), eq(ContentDocument.class)))
                .thenThrow(new IOException("Connection refused"));

        List<SearchResult> results = searchService.search("测试", 1, 10, "relevance");

        assertThat(results).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnEmptyWhenNullHits() throws Exception {
        SearchResponse<ContentDocument> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(null);

        when(esClient.search(any(SearchRequest.class), eq(ContentDocument.class)))
                .thenReturn(searchResponse);

        List<SearchResult> results = searchService.search("测试", 1, 10, "relevance");

        assertThat(results).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldHybridSearchWithBothResults() throws Exception {
        Hit<ContentDocument> hit = mock(Hit.class);
        when(hit.id()).thenReturn("1");

        HitsMetadata<ContentDocument> hitsMetadata = mock(HitsMetadata.class);
        when(hitsMetadata.hits()).thenReturn(List.of(hit));

        SearchResponse<ContentDocument> searchResponse = mock(SearchResponse.class);
        when(searchResponse.hits()).thenReturn(hitsMetadata);

        when(esClient.search(any(SearchRequest.class), eq(ContentDocument.class)))
                .thenReturn(searchResponse);
        when(vectorSearchService.search("测试", 50)).thenReturn(List.of(1L, 2L));

        List<SearchResult> results = searchService.hybridSearch("测试", 1, 10);

        assertThat(results).isNotEmpty();
    }

    @Test
    void shouldHybridSearchWithOnlyVectorResults() throws Exception {
        when(esClient.search(any(SearchRequest.class), eq(ContentDocument.class)))
                .thenThrow(new IOException("ES down"));
        when(vectorSearchService.search("测试", 50)).thenReturn(List.of(1L, 2L, 3L));

        List<SearchResult> results = searchService.hybridSearch("测试", 1, 10);

        assertThat(results).hasSize(3);
    }
}
