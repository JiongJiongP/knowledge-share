package com.company.search.application.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.company.search.TestConfig.class)
@ActiveProfiles("test")
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Test
    void shouldReturnEmptyListWhenSearchFails() {
        var results = searchService.search("test", 1, 10, "relevance");
        assertThat(results).isEmpty();
    }

    @Test
    void shouldAttemptCreateIndexWithoutError() {
        searchService.createIndexIfNotExists();
        // No exception thrown
    }

    @Test
    void shouldHandleIndexContentGracefully() {
        searchService.indexContent(1L, "title", "body", "MARKDOWN", "PUBLISHED", "1", "2024-01-01T00:00:00");
        // No exception thrown
    }

    @Test
    void shouldHandleDeleteContentGracefully() {
        searchService.deleteContent(1L);
        // No exception thrown
    }

    @Test
    void shouldReturnEmptyResultsForHybridSearch() {
        var results = searchService.hybridSearch("test", 1, 10);
        assertThat(results).isEmpty();
    }
}
