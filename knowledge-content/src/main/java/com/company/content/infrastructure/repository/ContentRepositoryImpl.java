package com.company.content.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.content.infrastructure.mapper.ContentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ContentRepositoryImpl implements ContentRepository {

    private final ContentMapper mapper;

    public ContentRepositoryImpl(ContentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public KnowledgeContent findById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public List<KnowledgeContent> findPublished(int page, int size, String sort, String contentType, String keyword) {
        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
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

        return mapper.selectPage(new Page<>(page, size, false), qw).getRecords();
    }

    @Override
    public long countPublished(String contentType, String keyword) {
        LambdaQueryWrapper<KnowledgeContent> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeContent::getStatus, PublishStatus.PUBLISHED);

        if (contentType != null && !contentType.isBlank()) {
            qw.eq(KnowledgeContent::getContentType, parseContentType(contentType));
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like(KnowledgeContent::getTitle, keyword)
                         .or()
                         .like(KnowledgeContent::getBody, keyword));
        }

        return mapper.selectCount(qw);
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
