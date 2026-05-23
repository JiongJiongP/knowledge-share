package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.ContentTagRelation;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.Tag;
import com.company.content.domain.repository.ContentRepository;
import com.company.content.domain.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ContentRepository contentRepository;

    public TagService(TagRepository tagRepository, ContentRepository contentRepository) {
        this.tagRepository = tagRepository;
        this.contentRepository = contentRepository;
    }

    public List<Tag> listAll() {
        return tagRepository.findAll();
    }

    public List<Tag> listByContentId(Long contentId) {
        return tagRepository.findByContentId(contentId);
    }

    public Tag create(String name, String color, Long userId) {
        Tag existing = tagRepository.findByName(name);
        if (existing != null) {
            throw BizException.badRequest("标签名称已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setColor(color != null ? color : "#409EFF");
        tag.setCreatedBy(userId);
        tagRepository.insert(tag);
        return tag;
    }

    public Tag update(Long id, String name, String color) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw BizException.notFound("标签");
        }
        Tag dup = tagRepository.findByName(name);
        if (dup != null && !dup.getId().equals(id)) {
            throw BizException.badRequest("标签名称已存在");
        }
        tag.setName(name);
        tag.setColor(color);
        tagRepository.update(tag);
        return tag;
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id);
        if (tag == null) {
            throw BizException.notFound("标签");
        }
        tagRepository.deleteRelationsByTagId(id);
        tagRepository.delete(id);
    }

    @Transactional
    public void setContentTags(Long contentId, List<Long> tagIds, Long userId) {
        KnowledgeContent content = contentRepository.findById(contentId);
        if (content == null) {
            throw BizException.notFound("内容");
        }
        if (!content.getCreatedBy().equals(userId)) {
            throw BizException.forbidden();
        }
        tagRepository.deleteRelationsByContentId(contentId);
        for (Long tagId : tagIds) {
            ContentTagRelation rel = new ContentTagRelation();
            rel.setContentId(contentId);
            rel.setTagId(tagId);
            tagRepository.insertRelation(rel);
        }
    }
}
