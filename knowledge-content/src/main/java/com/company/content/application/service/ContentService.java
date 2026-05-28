package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.util.UserDisplayUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserMapper userMapper;

    public ContentService(ContentRepository contentRepository, UserMapper userMapper) {
        this.contentRepository = contentRepository;
        this.userMapper = userMapper;
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
        List<KnowledgeContent> list = contentRepository.findPublished(page, size, sort, contentType, keyword);
        enrichCreatedByName(list);
        return PageResult.of(list, contentRepository.countPublished(contentType, keyword), page, size);
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
