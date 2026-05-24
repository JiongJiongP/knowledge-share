package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
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
        return PageResult.of(
                contentRepository.findPublished(page, size, sort, contentType, keyword),
                contentRepository.countPublished(contentType, keyword),
                page, size
        );
    }

    public KnowledgeContent getAccessible(Long id, Long userId) {
        KnowledgeContent c = contentRepository.findById(id);
        if (c == null) {
            throw BizException.notFound("内容");
        }
        if (c.getStatus() != PublishStatus.PUBLISHED && !c.getCreatedBy().equals(userId)) {
            throw BizException.notFound("内容");
        }
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
    }

    @Transactional
    public void softDelete(Long id, Long userId) {
        getOwned(id, userId);
        contentRepository.softDelete(id);
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
}
