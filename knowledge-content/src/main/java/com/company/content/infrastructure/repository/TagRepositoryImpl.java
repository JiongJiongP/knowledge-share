package com.company.content.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.content.domain.model.ContentTagRelation;
import com.company.content.domain.model.Tag;
import com.company.content.domain.repository.TagRepository;
import com.company.content.infrastructure.mapper.ContentTagRelationMapper;
import com.company.content.infrastructure.mapper.TagMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TagRepositoryImpl implements TagRepository {

    private final TagMapper tagMapper;
    private final ContentTagRelationMapper relationMapper;

    public TagRepositoryImpl(TagMapper tagMapper, ContentTagRelationMapper relationMapper) {
        this.tagMapper = tagMapper;
        this.relationMapper = relationMapper;
    }

    @Override
    public List<Tag> findAll() {
        return tagMapper.selectList(
            new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getCreatedAt)
        );
    }

    @Override
    public Tag findById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public Tag findByName(String name) {
        return tagMapper.selectOne(
            new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)
        );
    }

    @Override
    public void insert(Tag tag) {
        tagMapper.insert(tag);
    }

    @Override
    public void update(Tag tag) {
        tagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    @Override
    public List<Tag> findByContentId(Long contentId) {
        List<ContentTagRelation> relations = relationMapper.selectList(
            new LambdaQueryWrapper<ContentTagRelation>()
                .eq(ContentTagRelation::getContentId, contentId)
        );
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = relations.stream().map(ContentTagRelation::getTagId).toList();
        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    public void insertRelation(ContentTagRelation relation) {
        relationMapper.insert(relation);
    }

    @Override
    public void deleteRelationsByContentId(Long contentId) {
        relationMapper.delete(
            new LambdaQueryWrapper<ContentTagRelation>()
                .eq(ContentTagRelation::getContentId, contentId)
        );
    }

    @Override
    public void deleteRelationsByTagId(Long tagId) {
        relationMapper.delete(
            new LambdaQueryWrapper<ContentTagRelation>()
                .eq(ContentTagRelation::getTagId, tagId)
        );
    }
}
