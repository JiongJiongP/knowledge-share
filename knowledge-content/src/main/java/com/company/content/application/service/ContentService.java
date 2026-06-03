package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.search.application.dto.SearchResult;
import com.company.search.application.service.SearchService;
import com.company.search.application.service.VectorSearchService;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.util.UserDisplayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private final ContentRepository contentRepository;
    private final UserMapper userMapper;
    private final SearchService searchService;
    private final VectorSearchService vectorSearchService;

    public ContentService(ContentRepository contentRepository, UserMapper userMapper,
                          SearchService searchService, VectorSearchService vectorSearchService) {
        this.contentRepository = contentRepository;
        this.userMapper = userMapper;
        this.searchService = searchService;
        this.vectorSearchService = vectorSearchService;
    }

    @Transactional
    public KnowledgeContent create(Long userId, CreateContentRequest req) {
        KnowledgeContent c = new KnowledgeContent();
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        c.setContentType(req.getContentType());
        c.setStatus(PublishStatus.DRAFT);
        c.setCreatedBy(userId);
        contentRepository.insert(c);
        return c;
    }

    public PageResult<KnowledgeContent> listPublished(int page, int size, String sort, String contentType, String keyword) {
        long t0 = System.currentTimeMillis();

        // 关键词搜索走 Elasticsearch，无关键词走 MySQL
        boolean useEs = keyword != null && !keyword.isBlank();
        List<KnowledgeContent> list;
        long total;

        if (useEs) {
            try {
                List<SearchResult> esResults = searchService.search(keyword, page, size, sort);
                long t1 = System.currentTimeMillis();

                if (!esResults.isEmpty()) {
                    List<Long> ids = esResults.stream().map(SearchResult::getId).collect(Collectors.toList());
                    List<KnowledgeContent> fromDb = contentRepository.findByIds(ids);
                    long t2 = System.currentTimeMillis();

                    // 按 ES 返回顺序重排
                    Map<Long, KnowledgeContent> idToContent = fromDb.stream()
                            .collect(Collectors.toMap(KnowledgeContent::getId, c -> c, (a, b) -> a));
                    list = ids.stream()
                            .map(idToContent::get)
                            .filter(c -> c != null && c.getStatus() == PublishStatus.PUBLISHED)
                            .collect(Collectors.toList());

                    total = searchService.getLastSearchTotal(); // 用 ES 命中总数，避免 MySQL title LIKE 慢查询
                    long t3 = System.currentTimeMillis();

                    log.info("[listPublished] ES search | keyword={}, esHits={}, dbLoaded={}, findPublished={}ms, loadByIds={}ms, count={}ms, total={}ms",
                            keyword, esResults.size(), fromDb.size(), t1 - t0, t2 - t1, t3 - t2, t3 - t0);
                    enrichCreatedByName(list);
                    return PageResult.of(list, total, page, size);
                }
                log.info("[listPublished] ES returned empty, falling back to MySQL | keyword={}, time={}ms", keyword, t1 - t0);
            } catch (Exception e) {
                log.warn("[listPublished] ES search failed, falling back to MySQL | keyword={}, error={}", keyword, e.getMessage());
            }
            // ES 不可用或无结果时降级到 MySQL Like
            list = contentRepository.findPublished(page, size, sort, contentType, keyword);
            long t1 = System.currentTimeMillis();
            total = contentRepository.countPublished(contentType, keyword);
            log.info("[listPublished] MySQL fallback | findPublished={}ms, countPublished={}ms", t1 - t0, System.currentTimeMillis() - t1);
        } else {
            list = contentRepository.findPublished(page, size, sort, contentType, keyword);
            long t1 = System.currentTimeMillis();

            total = contentRepository.countPublished(contentType, keyword);
            long t2 = System.currentTimeMillis();

            log.info("[listPublished] MySQL | findPublished={}ms, countPublished={}ms, total={}ms", t1 - t0, t2 - t1, t2 - t0);
        }

        enrichCreatedByName(list);
        return PageResult.of(list, total, page, size);
    }

    private void enrichCreatedByName(List<KnowledgeContent> list) {
        if (list.isEmpty()) return;
        List<Long> userIds = list.stream().map(KnowledgeContent::getCreatedBy).distinct().collect(Collectors.toList());
        Map<Long, String> userNameMap = userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .in(User::getId, userIds)
        ).stream().collect(Collectors.toMap(User::getId, UserDisplayUtil::resolve, (a, b) -> a));
        for (KnowledgeContent c : list) {
            c.setCreatedByName(userNameMap.getOrDefault(c.getCreatedBy(), String.valueOf(c.getCreatedBy())));
        }
    }

    public KnowledgeContent getAccessible(Long id, Long userId) {
        KnowledgeContent c = contentRepository.findById(id);
        if (c == null) {
            throw BizException.notFound("内容");
        }
        if (c.getStatus() != PublishStatus.PUBLISHED && !c.getCreatedBy().equals(userId)) {
            throw BizException.notFound("内容");
        }
        enrichCreatedByName(List.of(c));
        return c;
    }

    private KnowledgeContent getOwned(Long id, Long userId) {
        KnowledgeContent c = contentRepository.findById(id);
        if (c == null) {
            throw BizException.notFound("内容");
        }
        if (!c.getCreatedBy().equals(userId)) {
            throw BizException.forbidden();
        }
        return c;
    }

    @Transactional
    public KnowledgeContent update(Long id, Long userId, CreateContentRequest req) {
        KnowledgeContent c = getOwned(id, userId);
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        c.setContentType(req.getContentType());
        contentRepository.update(c);
        // 已发布的内容更新后重新同步到搜索引擎
        if (c.getStatus() == PublishStatus.PUBLISHED) {
            syncToSearchEngine(c);
        }
        return c;
    }

    @Transactional
    public void publish(Long id, Long userId) {
        KnowledgeContent c = getOwned(id, userId);
        if (c.getStatus() == PublishStatus.PUBLISHED) {
            throw BizException.badRequest("内容已发布");
        }
        c.setStatus(PublishStatus.PUBLISHED);
        c.setPublishedAt(LocalDateTime.now());
        contentRepository.update(c);
        // 发布后同步到 ES + 向量数据库
        syncToSearchEngine(c);
    }

    @Transactional
    public void softDelete(Long id, Long userId) {
        getOwned(id, userId);
        contentRepository.softDelete(id);
        // 从搜索引擎移除
        removeFromSearchEngine(id);
    }

    @Transactional
    public void saveDraft(Long id, Long userId, CreateContentRequest req) {
        KnowledgeContent c = getOwned(id, userId);
        if (c.getStatus() != PublishStatus.DRAFT) {
            throw BizException.badRequest("仅草稿可保存");
        }
        c.setTitle(req.getTitle());
        c.setBody(req.getBody());
        contentRepository.update(c);
    }

    /**
     * 异步全量重建 ES + Qdrant 索引
     */
    public void reindexAllPublishedAsync() {
        new Thread(() -> {
            int total = 0;
            int page = 1;
            int batchSize = 200;
            while (true) {
                List<KnowledgeContent> batch = contentRepository.findPublished(page, batchSize, "latest", null, null);
                if (batch.isEmpty()) break;

                // ES 批量索引（一次 HTTP 请求处理一整批）
                try {
                    List<Long> ids = new ArrayList<>();
                    List<String> titles = new ArrayList<>();
                    List<String> bodies = new ArrayList<>();
                    List<String> types = new ArrayList<>();
                    List<String> statuses = new ArrayList<>();
                    List<String> authors = new ArrayList<>();
                    List<String> dates = new ArrayList<>();
                    for (KnowledgeContent c : batch) {
                        ids.add(c.getId());
                        titles.add(c.getTitle());
                        bodies.add(c.getBody());
                        types.add(c.getContentType() != null ? c.getContentType().name() : null);
                        statuses.add(c.getStatus() != null ? c.getStatus().name() : null);
                        authors.add(String.valueOf(c.getCreatedBy()));
                        dates.add(c.getPublishedAt() != null ? c.getPublishedAt().toString() : null);
                    }
                    searchService.batchIndex(ids, titles, bodies, types, statuses, authors, dates);
                } catch (Exception e) {
                    log.warn("[reindex] ES batch failed page={}: {}", page, e.getMessage());
                }

                // Qdrant 批量写入（已自动跳过不可用状态）
                try {
                    List<Long> ids = new ArrayList<>();
                    List<String> titles = new ArrayList<>();
                    List<String> bodies = new ArrayList<>();
                    for (KnowledgeContent c : batch) {
                        ids.add(c.getId());
                        titles.add(c.getTitle());
                        bodies.add(c.getBody());
                    }
                    vectorSearchService.batchUpsert(ids, titles, bodies);
                } catch (Exception e) {
                    log.debug("[reindex] Qdrant batch failed page={}: {}", page, e.getMessage());
                }

                total += batch.size();
                log.info("[reindex] page={}, batch={}, total={}", page, batch.size(), total);
                if (batch.size() < batchSize) break;
                page++;
            }
            log.info("[reindex] DONE — total synced: {}", total);
        }, "reindex-worker").start();
    }

    // ── 搜索引擎同步 ──

    private void syncToSearchEngine(KnowledgeContent c) {
        String publishedAt = c.getPublishedAt() != null ? c.getPublishedAt().toString() : null;
        try {
            searchService.indexContent(c.getId(), c.getTitle(), c.getBody(),
                    c.getContentType() != null ? c.getContentType().name() : null,
                    c.getStatus() != null ? c.getStatus().name() : null,
                    String.valueOf(c.getCreatedBy()), publishedAt);
        } catch (Exception e) {
            log.warn("ES indexContent failed for content {}: {}", c.getId(), e.getMessage());
        }
        try {
            vectorSearchService.upsert(c.getId(), c.getTitle(), c.getBody());
        } catch (Exception e) {
            log.warn("Qdrant upsert failed for content {}: {}", c.getId(), e.getMessage());
        }
    }

    private void removeFromSearchEngine(Long id) {
        try {
            searchService.deleteContent(id);
        } catch (Exception e) {
            log.warn("ES deleteContent failed for content {}: {}", id, e.getMessage());
        }
        try {
            vectorSearchService.delete(id);
        } catch (Exception e) {
            log.warn("Qdrant delete failed for content {}: {}", id, e.getMessage());
        }
    }
}
