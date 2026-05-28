package com.company.search.application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.company.search.application.dto.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceUnitTest {

    @Mock
    private ElasticsearchClient esClient;

    @Mock
    private VectorSearchService vectorSearchService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void shouldReturnEmptyHybridSearchWhenNoResults() {
        when(vectorSearchService.search("test", 50)).thenReturn(Collections.emptyList());

        List<SearchResult> results = searchService.hybridSearch("test", 1, 10);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldPaginateHybridSearchResults() {
        when(vectorSearchService.search("test", 50)).thenReturn(List.of(1L, 2L, 3L));

        List<SearchResult> page1 = searchService.hybridSearch("test", 1, 2);
        assertThat(page1).hasSize(2);
        assertThat(page1.get(0).getId()).isEqualTo(1L);
        assertThat(page1.get(1).getId()).isEqualTo(2L);

        List<SearchResult> page2 = searchService.hybridSearch("test", 2, 2);
        assertThat(page2).hasSize(1);
        assertThat(page2.get(0).getId()).isEqualTo(3L);
    }

    @Test
    void shouldReturnEmptyPageBeyondResults() {
        when(vectorSearchService.search("test", 50)).thenReturn(List.of(1L, 2L));

        List<SearchResult> page10 = searchService.hybridSearch("test", 10, 10);
        assertThat(page10).isEmpty();
    }

    @Test
    void shouldHandleSingleResultHybridSearch() {
        when(vectorSearchService.search("test", 50)).thenReturn(List.of(1L));

        List<SearchResult> results = searchService.hybridSearch("test", 1, 10);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }
}
