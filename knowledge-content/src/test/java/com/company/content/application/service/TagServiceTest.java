package com.company.content.application.service;

import com.company.common.exception.BizException;
import com.company.content.domain.model.ContentTagRelation;
import com.company.content.domain.model.KnowledgeContent;
import com.company.content.domain.model.Tag;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import com.company.content.domain.repository.ContentRepository;
import com.company.content.domain.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private TagService tagService;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag();
        tag.setId(1L);
        tag.setName("技术");
        tag.setColor("#409EFF");
        tag.setCreatedBy(1L);
    }

    @Test
    void shouldCreateTag() {
        when(tagRepository.findByName("技术")).thenReturn(null);
        doAnswer(inv -> { Tag t = inv.getArgument(0); t.setId(1L); return null; })
            .when(tagRepository).insert(any());

        Tag result = tagService.create("技术", "#409EFF", 1L);

        assertThat(result.getName()).isEqualTo("技术");
        assertThat(result.getCreatedBy()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenTagNameExists() {
        when(tagRepository.findByName("技术")).thenReturn(tag);

        assertThatThrownBy(() -> tagService.create("技术", "#409EFF", 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void shouldUpdateTag() {
        when(tagRepository.findById(1L)).thenReturn(tag);
        when(tagRepository.findByName("产品")).thenReturn(null);

        tagService.update(1L, "产品", "#67C23A");

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository).update(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("产品");
        assertThat(captor.getValue().getColor()).isEqualTo("#67C23A");
    }

    @Test
    void shouldThrowWhenUpdatingToDuplicateName() {
        Tag another = new Tag();
        another.setId(2L);
        another.setName("产品");
        when(tagRepository.findById(1L)).thenReturn(tag);
        when(tagRepository.findByName("产品")).thenReturn(another);

        assertThatThrownBy(() -> tagService.update(1L, "产品", "#67C23A"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void shouldDeleteTag() {
        when(tagRepository.findById(1L)).thenReturn(tag);

        tagService.delete(1L);

        verify(tagRepository).deleteRelationsByTagId(1L);
        verify(tagRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentTag() {
        when(tagRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> tagService.delete(999L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    void shouldListAll() {
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<Tag> result = tagService.listAll();
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldSetContentTags() {
        KnowledgeContent content = new KnowledgeContent();
        content.setId(1L);
        content.setTitle("Test");
        content.setContentType(ContentType.MARKDOWN);
        content.setStatus(PublishStatus.DRAFT);
        content.setCreatedBy(10L);
        when(contentRepository.findById(1L)).thenReturn(content);

        tagService.setContentTags(1L, List.of(1L, 2L), 10L);

        verify(tagRepository).deleteRelationsByContentId(1L);
        ArgumentCaptor<ContentTagRelation> captor = ArgumentCaptor.forClass(ContentTagRelation.class);
        verify(tagRepository, times(2)).insertRelation(captor.capture());
        List<ContentTagRelation> rels = captor.getAllValues();
        assertThat(rels.get(0).getTagId()).isEqualTo(1L);
        assertThat(rels.get(1).getTagId()).isEqualTo(2L);
    }

    @Test
    void shouldThrowWhenSettingTagsOnOtherUsersContent() {
        KnowledgeContent content = new KnowledgeContent();
        content.setId(1L);
        content.setCreatedBy(10L);
        when(contentRepository.findById(1L)).thenReturn(content);

        assertThatThrownBy(() -> tagService.setContentTags(1L, List.of(1L), 99L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }
}
