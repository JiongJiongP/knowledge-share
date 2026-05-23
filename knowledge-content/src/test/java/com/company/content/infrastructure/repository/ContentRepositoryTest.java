package com.company.content.infrastructure.repository;

import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = com.company.content.TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class ContentRepositoryTest {

    @Autowired
    private ContentRepository contentRepository;

    @Test
    void shouldInsertAndFindById() {
        KnowledgeContent content = new KnowledgeContent();
        content.setTitle("测试文章");
        content.setBody("这是正文内容");
        content.setContentType(ContentType.MARKDOWN);
        content.setStatus(PublishStatus.DRAFT);
        content.setCreatedBy(1L);

        contentRepository.insert(content);
        assertThat(content.getId()).isNotNull();

        KnowledgeContent found = contentRepository.findById(content.getId());
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("测试文章");
        assertThat(found.getBody()).isEqualTo("这是正文内容");
        assertThat(found.getContentType()).isEqualTo(ContentType.MARKDOWN);
        assertThat(found.getStatus()).isEqualTo(PublishStatus.DRAFT);
        assertThat(found.getCreatedBy()).isEqualTo(1L);
    }

    @Test
    void shouldFindPublishedWithPagination() {
        // Insert 5 draft and 3 published contents
        for (int i = 1; i <= 5; i++) {
            KnowledgeContent draft = new KnowledgeContent();
            draft.setTitle("草稿 " + i);
            draft.setBody("草稿内容");
            draft.setContentType(ContentType.MARKDOWN);
            draft.setStatus(PublishStatus.DRAFT);
            draft.setCreatedBy(1L);
            contentRepository.insert(draft);
        }
        for (int i = 1; i <= 3; i++) {
            KnowledgeContent published = new KnowledgeContent();
            published.setTitle("已发布 " + i);
            published.setBody("已发布内容");
            published.setContentType(ContentType.MARKDOWN);
            published.setStatus(PublishStatus.PUBLISHED);
            published.setCreatedBy(1L);
            published.setPublishedAt(LocalDateTime.now());
            contentRepository.insert(published);
        }

        List<KnowledgeContent> page1 = contentRepository.findPublished(1, 10, "latest", null, null);
        assertThat(page1).hasSize(3);
        assertThat(page1).allMatch(c -> c.getStatus() == PublishStatus.PUBLISHED);

        long count = contentRepository.countPublished(null, null);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void shouldUpdateContent() {
        KnowledgeContent content = new KnowledgeContent();
        content.setTitle("原始标题");
        content.setBody("原始内容");
        content.setContentType(ContentType.MARKDOWN);
        content.setStatus(PublishStatus.DRAFT);
        content.setCreatedBy(1L);
        contentRepository.insert(content);

        content.setTitle("修改后的标题");
        content.setBody("修改后的内容");
        contentRepository.update(content);

        KnowledgeContent updated = contentRepository.findById(content.getId());
        assertThat(updated.getTitle()).isEqualTo("修改后的标题");
        assertThat(updated.getBody()).isEqualTo("修改后的内容");
    }

    @Test
    void shouldSoftDelete() {
        KnowledgeContent content = new KnowledgeContent();
        content.setTitle("待删除内容");
        content.setBody("内容");
        content.setContentType(ContentType.MARKDOWN);
        content.setStatus(PublishStatus.DRAFT);
        content.setCreatedBy(1L);
        contentRepository.insert(content);

        contentRepository.softDelete(content.getId());

        KnowledgeContent found = contentRepository.findById(content.getId());
        assertThat(found).isNull();
    }

    @Test
    void shouldAutoFillTimestampsOnInsert() {
        KnowledgeContent content = new KnowledgeContent();
        content.setTitle("时间戳测试");
        content.setBody("测试自动填充");
        content.setContentType(ContentType.MARKDOWN);
        content.setStatus(PublishStatus.DRAFT);
        content.setCreatedBy(1L);

        contentRepository.insert(content);

        KnowledgeContent found = contentRepository.findById(content.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldReturnNullWhenNotFound() {
        KnowledgeContent found = contentRepository.findById(99999L);
        assertThat(found).isNull();
    }
}
