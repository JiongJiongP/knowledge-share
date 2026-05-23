package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.common.result.PageResult;
import com.company.content.application.dto.CreateContentRequest;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    private CreateContentRequest request;
    private KnowledgeContent draft;
    private KnowledgeContent published;

    @BeforeEach
    void setUp() {
        request = new CreateContentRequest();
        request.setTitle("测试标题");
        request.setBody("测试正文");
        request.setContentType(ContentType.MARKDOWN);

        draft = new KnowledgeContent();
        draft.setId(1L);
        draft.setTitle("测试标题");
        draft.setBody("测试正文");
        draft.setContentType(ContentType.MARKDOWN);
        draft.setStatus(PublishStatus.DRAFT);
        draft.setCreatedBy(1L);

        published = new KnowledgeContent();
        published.setId(2L);
        published.setTitle("已发布内容");
        published.setBody("正文");
        published.setContentType(ContentType.MARKDOWN);
        published.setStatus(PublishStatus.PUBLISHED);
        published.setCreatedBy(1L);
        published.setPublishedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateDraftContent() {
        doAnswer(inv -> {
            KnowledgeContent c = inv.getArgument(0);
            c.setId(1L);
            return null;
        }).when(contentRepository).insert(any());

        KnowledgeContent result = contentService.create(1L, request);

        assertThat(result.getTitle()).isEqualTo("测试标题");
        assertThat(result.getStatus()).isEqualTo(PublishStatus.DRAFT);
        assertThat(result.getCreatedBy()).isEqualTo(1L);
        verify(contentRepository).insert(any());
    }

    @Test
    void shouldAllowOwnerToAccessOwnDraft() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        KnowledgeContent result = contentService.getAccessible(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("测试标题");
    }

    @Test
    void shouldAllowAnyoneToAccessPublishedContent() {
        when(contentRepository.findById(2L)).thenReturn(published);

        KnowledgeContent result = contentService.getAccessible(2L, 99L);

        assertThat(result.getId()).isEqualTo(2L);
    }

    @Test
    void shouldHideDraftFromNonOwner() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        assertThatThrownBy(() -> contentService.getAccessible(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void shouldThrowWhenContentNotFound() {
        when(contentRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> contentService.getAccessible(999L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void shouldPublishDraftContent() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        contentService.publish(1L, 1L);

        ArgumentCaptor<KnowledgeContent> captor = ArgumentCaptor.forClass(KnowledgeContent.class);
        verify(contentRepository).update(captor.capture());
        KnowledgeContent updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(PublishStatus.PUBLISHED);
        assertThat(updated.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenPublishingAlreadyPublished() {
        when(contentRepository.findById(2L)).thenReturn(published);

        assertThatThrownBy(() -> contentService.publish(2L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已发布");
    }

    @Test
    void shouldThrowWhenNonAuthorPublishes() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        assertThatThrownBy(() -> contentService.publish(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldUpdateContent() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        CreateContentRequest updateReq = new CreateContentRequest();
        updateReq.setTitle("修改后的标题");
        updateReq.setBody("修改后的正文");
        updateReq.setContentType(ContentType.EXTERNAL_URL);

        contentService.update(1L, 1L, updateReq);

        ArgumentCaptor<KnowledgeContent> captor = ArgumentCaptor.forClass(KnowledgeContent.class);
        verify(contentRepository).update(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("修改后的标题");
        assertThat(captor.getValue().getContentType()).isEqualTo(ContentType.EXTERNAL_URL);
    }

    @Test
    void shouldThrowWhenNonAuthorUpdates() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        assertThatThrownBy(() -> contentService.update(1L, 2L, request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldSoftDelete() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        contentService.softDelete(1L, 1L);

        verify(contentRepository).softDelete(1L);
    }

    @Test
    void shouldThrowWhenNonAuthorDeletes() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        assertThatThrownBy(() -> contentService.softDelete(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldSaveDraft() {
        when(contentRepository.findById(1L)).thenReturn(draft);

        CreateContentRequest draftReq = new CreateContentRequest();
        draftReq.setTitle("草稿版标题");
        draftReq.setBody("草稿正文");
        draftReq.setContentType(ContentType.MARKDOWN);

        contentService.saveDraft(1L, 1L, draftReq);

        ArgumentCaptor<KnowledgeContent> captor = ArgumentCaptor.forClass(KnowledgeContent.class);
        verify(contentRepository).update(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("草稿版标题");
    }

    @Test
    void shouldThrowWhenSaveDraftOnPublished() {
        when(contentRepository.findById(2L)).thenReturn(published);

        assertThatThrownBy(() -> contentService.saveDraft(2L, 1L, request))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("仅草稿可保存");
    }

    @Test
    void shouldListPublished() {
        when(contentRepository.findPublished(1, 10, "latest", null, null)).thenReturn(List.of(published));
        when(contentRepository.countPublished(null, null)).thenReturn(1L);

        PageResult<KnowledgeContent> result = contentService.listPublished(1, 10, "latest", null, null);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }
}
