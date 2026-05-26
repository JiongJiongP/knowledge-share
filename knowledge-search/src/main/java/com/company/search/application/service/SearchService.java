package com.company.search.application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import com.company.search.application.dto.ContentDocument;
import com.company.search.application.dto.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final String INDEX_NAME = "knowledge_content";
    private static final int RRF_K = 60;

    private final ElasticsearchClient esClient;
    private final VectorSearchService vectorSearchService;

    public SearchService(ElasticsearchClient esClient, VectorSearchService vectorSearchService) {
        this.esClient = esClient;
        this.vectorSearchService = vectorSearchService;
    }

    public void createIndexIfNotExists() {
        try {
            var indices = esClient.indices();
            if (indices == null) return;
            boolean exists = indices.exists(e -> e.index(INDEX_NAME)).value();
            if (!exists) {
                esClient.indices().create(CreateIndexRequest.of(c -> c
                        .index(INDEX_NAME)
                        .settings(IndexSettings.of(s -> s
                                .numberOfShards("1")
                                .numberOfReplicas("0")
                        ))
                        .mappings(m -> m
                                .properties("title", p -> p.text(t -> t.analyzer("ik_max_word").searchAnalyzer("ik_smart")))
                                .properties("body", p -> p.text(t -> t.analyzer("ik_max_word").searchAnalyzer("ik_smart")))
                                .properties("contentType", p -> p.keyword(k -> k))
                                .properties("createdBy", p -> p.keyword(k -> k))
                                .properties("publishedAt", p -> p.date(d -> d))
                        )
                ));
                log.info("Elasticsearch index '{}' created", INDEX_NAME);
            }
        } catch (IOException e) {
            log.warn("Failed to create Elasticsearch index: {}", e.getMessage());
        }
    }

    public void indexContent(Long id, String title, String body, String contentType, String createdBy, String publishedAt) {
        try {
            ContentDocument doc = new ContentDocument();
            doc.setTitle(title);
            doc.setBody(body);
            doc.setContentType(contentType);
            doc.setCreatedBy(createdBy);
            doc.setPublishedAt(publishedAt);

            esClient.index(IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(id))
                    .document(doc)
            ));
        } catch (IOException e) {
            log.warn("Failed to index content {}: {}", id, e.getMessage());
        }
    }

    public void deleteContent(Long id) {
        try {
            esClient.delete(DeleteRequest.of(d -> d
                    .index(INDEX_NAME)
                    .id(String.valueOf(id))
            ));
        } catch (IOException e) {
            log.warn("Failed to delete content {} from index: {}", id, e.getMessage());
        }
    }

    public void batchIndex(List<Long> ids, List<String> titles, List<String> bodies,
                           List<String> contentTypes, List<String> createdBys, List<String> publishedAts) {
        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                ContentDocument doc = new ContentDocument();
                doc.setTitle(titles.get(i));
                doc.setBody(bodies.get(i));
                doc.setContentType(contentTypes.get(i));
                doc.setCreatedBy(createdBys.get(i));
                doc.setPublishedAt(publishedAts.get(i));
                final int idx = i;
                operations.add(BulkOperation.of(op -> op
                        .index(ix -> ix
                                .index(INDEX_NAME)
                                .id(String.valueOf(ids.get(idx)))
                                .document(doc))));
            }
            esClient.bulk(BulkRequest.of(b -> b.operations(operations)));
        } catch (IOException e) {
            log.warn("Failed to batch index {} documents: {}", ids.size(), e.getMessage());
        }
    }

    public List<SearchResult> search(String keyword, int page, int size, String sort) {
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .from((page - 1) * size)
                    .size(size)
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .fields("title", "body")
                                    .query(keyword)
                            )
                    )
                    .highlight(h -> h
                            .fields("body", f -> f.fragmentSize(100).numberOfFragments(1))
                    )
                    .sort(sort_ -> sort_.score(sc -> sc.order(SortOrder.Desc)))
            );

            SearchResponse<ContentDocument> response = esClient.search(request, ContentDocument.class);
            if (response == null || response.hits() == null) {
                return Collections.emptyList();
            }

            List<SearchResult> results = new ArrayList<>();
            for (Hit<ContentDocument> hit : response.hits().hits()) {
                SearchResult r = SearchResult.fromSource(Long.valueOf(hit.id()), hit);
                results.add(r);
            }
            return results;
        } catch (IOException e) {
            log.warn("Search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<SearchResult> hybridSearch(String keyword, int page, int size) {
        // Get ranked IDs from both search engines
        Map<Long, Integer> esRanks = keywordRankedIds(keyword, 50);
        List<Long> vectorIds = vectorSearchService.search(keyword, 50);
        Map<Long, Integer> vectorRanks = new HashMap<>();
        for (int i = 0; i < vectorIds.size(); i++) {
            vectorRanks.putIfAbsent(vectorIds.get(i), i + 1);
        }

        // RRF fusion: score = Σ 1/(k + rank_i)
        Map<Long, Double> rrfScores = new HashMap<>();
        for (Map.Entry<Long, Integer> e : esRanks.entrySet()) {
            rrfScores.merge(e.getKey(), 1.0 / (RRF_K + e.getValue()), Double::sum);
        }
        for (Map.Entry<Long, Integer> e : vectorRanks.entrySet()) {
            rrfScores.merge(e.getKey(), 1.0 / (RRF_K + e.getValue()), Double::sum);
        }

        // Sort by RRF score descending
        List<Map.Entry<Long, Double>> sorted = new ArrayList<>(rrfScores.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Paginate and build results
        int from = (page - 1) * size;
        int to = Math.min(from + size, sorted.size());
        List<SearchResult> results = new ArrayList<>();
        for (int i = from; i < to; i++) {
            Long id = sorted.get(i).getKey();
            SearchResult r = new SearchResult();
            r.setId(id);
            results.add(r);
        }
        return results;
    }

    private Map<Long, Integer> keywordRankedIds(String keyword, int topN) {
        Map<Long, Integer> ranks = new HashMap<>();
        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX_NAME)
                    .size(topN)
                    .query(q -> q
                            .multiMatch(mm -> mm
                                    .fields("title", "body")
                                    .query(keyword)
                            )
                    )
                    .sort(sort_ -> sort_.score(sc -> sc.order(SortOrder.Desc)))
            );

            SearchResponse<ContentDocument> response = esClient.search(request, ContentDocument.class);
            if (response == null || response.hits() == null) return ranks;

            int rank = 1;
            for (Hit<ContentDocument> hit : response.hits().hits()) {
                ranks.put(Long.valueOf(hit.id()), rank++);
            }
        } catch (Exception e) {
            log.warn("Keyword ranking failed: {}", e.getMessage());
        }
        return ranks;
    }
}
