package com.company.content.domain.repository;

import com.company.content.domain.model.KnowledgeContent;

import java.util.List;

public interface ContentRepository {
    KnowledgeContent findById(Long id);
    List<KnowledgeContent> findByIds(List<Long> ids);
    List<KnowledgeContent> findPublished(int page, int size, String sort, String contentType, String keyword);
    long countPublished(String contentType, String keyword);
    void insert(KnowledgeContent content);
    void update(KnowledgeContent content);
    void softDelete(Long id);
}
