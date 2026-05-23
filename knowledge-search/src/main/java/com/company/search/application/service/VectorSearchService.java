package com.company.search.application.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);
    private static final String COLLECTION_NAME = "knowledge_content";

    private final QdrantClient qdrantClient;
    private final EmbeddingService embeddingService;

    public VectorSearchService(QdrantClient qdrantClient, EmbeddingService embeddingService) {
        this.qdrantClient = qdrantClient;
        this.embeddingService = embeddingService;
    }

    public void createCollectionIfNotExists() {
        if (qdrantClient == null) return;
        try {
            boolean exists = qdrantClient.collectionExistsAsync(COLLECTION_NAME).get();
            if (!exists) {
                qdrantClient.createCollectionAsync(
                        COLLECTION_NAME,
                        VectorParams.newBuilder()
                                .setSize(embeddingService.dimension())
                                .setDistance(Distance.Cosine)
                                .build()
                ).get();
                log.info("Qdrant collection '{}' created", COLLECTION_NAME);
            }
        } catch (Exception e) {
            log.warn("Failed to create Qdrant collection: {}", e.getMessage());
        }
    }

    public void upsert(Long id, String title, String body) {
        if (qdrantClient == null) return;
        try {
            float[] vec = embeddingService.embed(title + " " + body);
            log.debug("Upsert vector for content {}: dimension={}", id, vec.length);
            // TODO: Implement Qdrant upsert when the runtime API is available
            // qdrantClient.upsertAsync(COLLECTION_NAME, List.of(point)).get();
        } catch (Exception e) {
            log.warn("Failed to upsert vector for content {}: {}", id, e.getMessage());
        }
    }

    public void delete(Long id) {
        if (qdrantClient == null) return;
        try {
            log.debug("Delete vector for content {}", id);
            // TODO: Implement Qdrant delete when the runtime API is available
            // PointId pointId = PointId.newBuilder().setNum(id).build();
            // qdrantClient.deleteAsync(COLLECTION_NAME, List.of(pointId)).get();
        } catch (Exception e) {
            log.warn("Failed to delete vector for content {}: {}", id, e.getMessage());
        }
    }

    public List<Long> search(String query, int topK) {
        if (qdrantClient == null) return Collections.emptyList();
        try {
            float[] vec = embeddingService.embed(query);
            log.debug("Vector search: dimension={}, topK={}", vec.length, topK);
            // TODO: Implement Qdrant search when the runtime API is available
            // SearchPoints request = SearchPoints.newBuilder()...
            // List<ScoredPoint> results = qdrantClient.searchAsync(request).get();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Vector search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
