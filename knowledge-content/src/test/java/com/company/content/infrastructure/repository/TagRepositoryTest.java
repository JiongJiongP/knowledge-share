package com.company.content.infrastructure.repository;

import com.company.content.domain.model.ContentTagRelation;
import com.company.content.domain.model.Tag;
import com.company.content.domain.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.company.content.TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void shouldInsertAndFindTag() {
        Tag tag = new Tag();
        tag.setName("技术");
        tag.setColor("#409EFF");
        tag.setCreatedBy(1L);
        tagRepository.insert(tag);
        assertThat(tag.getId()).isNotNull();

        Tag found = tagRepository.findById(tag.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("技术");
    }

    @Test
    void shouldFindByName() {
        Tag tag = new Tag();
        tag.setName("产品");
        tag.setColor("#67C23A");
        tag.setCreatedBy(1L);
        tagRepository.insert(tag);

        Tag found = tagRepository.findByName("产品");
        assertThat(found).isNotNull();
        assertThat(found.getColor()).isEqualTo("#67C23A");
    }

    @Test
    void shouldFindAll() {
        for (String name : List.of("Java", "Python", "Go")) {
            Tag tag = new Tag();
            tag.setName(name);
            tag.setColor("#409EFF");
            tag.setCreatedBy(1L);
            tagRepository.insert(tag);
        }

        List<Tag> all = tagRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void shouldDeleteTag() {
        Tag tag = new Tag();
        tag.setName("临时");
        tag.setColor("#409EFF");
        tag.setCreatedBy(1L);
        tagRepository.insert(tag);

        tagRepository.delete(tag.getId());

        Tag found = tagRepository.findById(tag.getId());
        assertThat(found).isNull();
    }

    @Test
    void shouldFindTagsByContentId() {
        Tag t1 = new Tag(); t1.setName("Java"); t1.setColor("#409EFF"); t1.setCreatedBy(1L);
        Tag t2 = new Tag(); t2.setName("Spring"); t2.setColor("#67C23A"); t2.setCreatedBy(1L);
        tagRepository.insert(t1);
        tagRepository.insert(t2);

        for (Tag t : List.of(t1, t2)) {
            ContentTagRelation rel = new ContentTagRelation();
            rel.setContentId(100L);
            rel.setTagId(t.getId());
            tagRepository.insertRelation(rel);
        }

        List<Tag> tags = tagRepository.findByContentId(100L);
        assertThat(tags).hasSize(2);

        List<Tag> empty = tagRepository.findByContentId(999L);
        assertThat(empty).isEmpty();
    }

    @Test
    void shouldDeleteRelationsByContentId() {
        Tag t1 = new Tag(); t1.setName("Test"); t1.setColor("#409EFF"); t1.setCreatedBy(1L);
        tagRepository.insert(t1);

        ContentTagRelation rel = new ContentTagRelation();
        rel.setContentId(200L);
        rel.setTagId(t1.getId());
        tagRepository.insertRelation(rel);

        tagRepository.deleteRelationsByContentId(200L);

        List<Tag> tags = tagRepository.findByContentId(200L);
        assertThat(tags).isEmpty();
    }
}
