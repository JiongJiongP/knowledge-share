package com.company.search.application.service;

import io.qdrant.client.PointIdFactory;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.ValueFactory;
import io.qdrant.client.VectorsFactory;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
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
            PointStruct point = PointStruct.newBuilder()
                    .setId(PointIdFactory.id(id))
                    .setVectors(VectorsFactory.vectors(vec))
                    .putPayload("title", ValueFactory.value(title))
                    .putPayload("body", ValueFactory.value(body))
                    .build();
            qdrantClient.upsertAsync(COLLECTION_NAME, List.of(point)).get();
        } catch (Exception e) {
            log.warn("Failed to upsert vector for content {}: {}", id, e.getMessage());
        }
    }

    public void batchUpsert(List<Long> ids, List<String> titles, List<String> bodies) {
        if (qdrantClient == null || ids.isEmpty()) return;
        try {
            List<PointStruct> points = new ArrayList<>(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                float[] vec = embeddingService.embed(titles.get(i) + " " + bodies.get(i));
                points.add(PointStruct.newBuilder()
                        .setId(PointIdFactory.id(ids.get(i)))
                        .setVectors(VectorsFactory.vectors(vec))
                        .putPayload("title", ValueFactory.value(titles.get(i)))
                        .putPayload("body", ValueFactory.value(bodies.get(i)))
                        .build());
            }
            qdrantClient.upsertAsync(COLLECTION_NAME, points).get();
        } catch (Exception e) {
            log.warn("Failed to batch upsert {} vectors: {}", ids.size(), e.getMessage());
        }
    }

    public void delete(Long id) {
        if (qdrantClient == null) return;
        try {
            qdrantClient.deleteAsync(COLLECTION_NAME, List.of(PointIdFactory.id(id))).get();
        } catch (Exception e) {
            log.warn("Failed to delete vector for content {}: {}", id, e.getMessage());
        }
    }

    public List<Long> search(String query, int topK) {
        if (qdrantClient == null) return Collections.emptyList();
        try {
            float[] vec = embeddingService.embed(query);
            SearchPoints.Builder builder = SearchPoints.newBuilder()
                    .setCollectionName(COLLECTION_NAME)
                    .setLimit(topK);
            for (float v : vec) {
                builder.addVector(v);
            }
            List<ScoredPoint> results = qdrantClient.searchAsync(builder.build()).get();
            return results.stream()
                    .map(p -> p.getId().getNum())
                    .toList();
        } catch (Exception e) {
            log.warn("Vector search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
