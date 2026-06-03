package com.company.content.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.content.infrastructure.mapper.ContentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class ContentRepositoryImpl implements ContentRepository {

    private static final Logger log = LoggerFactory.getLogger(ContentRepositoryImpl.class);

    private final ContentMapper mapper;

    public ContentRepositoryImpl(ContentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public KnowledgeContent findById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<KnowledgeContent> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return mapper.selectBatchIds(ids);
    }

    @Override
    public List<KnowledgeContent> findPublished(int page, int size, String sort, String contentType, String keyword) {
        long t0 = System.currentTimeMillis();

        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
        // 列表查询排除 body 大字段，大幅减少数据传输量
        qw.select(KnowledgeContent.class, info -> !"body".equals(info.getColumn()));
        qw.eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED);

        if (contentType != null && !contentType.isBlank()) {
            qw.eq(KnowledgeContent::getContentType, parseContentType(contentType));
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(KnowledgeContent::getTitle, keyword)
                         .or()
                         .like(KnowledgeContent::getBody, keyword));
        }

        if ("popular".equals(sort)) {
            qw.orderByDesc(KnowledgeContent::getCreatedAt);
        } else {
            qw.orderByDesc(KnowledgeContent::getPublishedAt);
        }

        List<KnowledgeContent> records = mapper.selectPage(new Page<>(page, size, false), qw).getRecords();
        log.info("[findPublished] page={}, size={}, sort={}, contentType={}, keyword={} | records={}, time={}ms",
                page, size, sort, contentType, keyword, records.size(), System.currentTimeMillis() - t0);
        return records;
    }

    @Override
    public long countPublished(String contentType, String keyword) {
        long t0 = System.currentTimeMillis();

        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED);

        if (contentType != null && !contentType.isBlank()) {
            qw.eq(KnowledgeContent::getContentType, parseContentType(contentType));
        }
        if (keyword != null && !keyword.isBlank()) {
            // 计数只匹配 title，避免 body TEXT 列全表扫描
            qw.like(KnowledgeContent::getTitle, keyword);
        }

        long count = mapper.selectCount(qw);
        log.info("[countPublished] contentType={}, keyword={} | count={}, time={}ms",
                contentType, keyword, count, System.currentTimeMillis() - t0);
        return count;
    }

    @Override
    public void insert(KnowledgeContent content) {
        mapper.insert(content);
    }

    @Override
    public void update(KnowledgeContent content) {
        mapper.updateById(content);
    }

    @Override
    public void softDelete(Long id) {
        mapper.deleteById(id);
    }

    private ContentType parseContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new com.company.common.exception.BizException(400, "无效的内容类型: " + contentType);
        }
    }
}
