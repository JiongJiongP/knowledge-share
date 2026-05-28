package com.company.search.application.service;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VectorSearchServiceTest {

    @Mock
    private QdrantClient qdrantClient;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private VectorSearchService vectorSearchService;

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
}
