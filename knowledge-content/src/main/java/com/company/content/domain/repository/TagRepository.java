package com.company.content.domain.repository;

import com.company.content.domain.model.ContentTagRelation;
import com.company.content.domain.model.Tag;

import java.util.List;

public interface TagRepository {
    List<Tag> findAll();
    Tag findById(Long id);
    Tag findByName(String name);
    void insert(Tag tag);
    void update(Tag tag);
    void delete(Long id);
    List<Tag> findByContentId(Long contentId);
    void insertRelation(ContentTagRelation relation);
    void deleteRelationsByContentId(Long contentId);
    void deleteRelationsByTagId(Long tagId);
}
