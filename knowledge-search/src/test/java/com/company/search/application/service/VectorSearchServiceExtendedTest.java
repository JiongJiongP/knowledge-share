package com.company.search.application.service;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VectorSearchServiceExtendedTest {

    @Mock
    private EmbeddingService embeddingService;

    @Test
    void shouldHandleNullQdrantClientForCreateCollection() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.createCollectionIfNotExists();
    }

    @Test
    void shouldHandleNullQdrantClientForUpsert() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.upsert(1L, "标题", "正文");
    }

    @Test
    void shouldHandleNullQdrantClientForBatchUpsert() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.batchUpsert(List.of(1L), List.of("标题"), List.of("正文"));
    }

    @Test
    void shouldHandleNullQdrantClientForDelete() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.delete(1L);
    }

    @Test
    void shouldHandleNullQdrantClientForSearch() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        List<Long> results = service.search("test", 10);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleEmptyBatchUpsert() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.batchUpsert(List.of(), List.of(), List.of());
    }

    @Test
    void shouldHandleNullClientWithEmptyBatch() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        service.batchUpsert(List.of(), List.of(), List.of());
    }

    @Test
    void shouldReturnEmptySearchResultsWithNullClient() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        List<Long> results = service.search("查询", 5);
        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptySearchResultsWithNullClientAndLargeTopK() {
        VectorSearchService service = new VectorSearchService(null, embeddingService);
        List<Long> results = service.search("test", 100);
        assertThat(results).isEmpty();
    }
}
