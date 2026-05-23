package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.social.domain.model.Comment;
import com.company.social.domain.model.CommentLike;
import com.company.social.domain.repository.CommentRepository;
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
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment comment;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        comment.setId(1L);
        comment.setContentId(100L);
        comment.setUserId(1L);
        comment.setBody("好文章");
        comment.setLikeCount(0);
        comment.setStatus("PUBLISHED");
    }

    @Test
    void shouldCreateComment() {
        doAnswer(inv -> { Comment c = inv.getArgument(0); c.setId(1L); return null; })
            .when(commentRepository).insert(any());

        Comment result = commentService.create(100L, 1L, "好文章",
                null, null, null, List.of(2L, 3L));

        assertThat(result.getBody()).isEqualTo("好文章");
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        verify(commentRepository, times(2)).insertMention(any());
    }

    @Test
    void shouldLikeComment() {
        when(commentRepository.findById(1L)).thenReturn(comment);
        when(commentRepository.findLike(1L, 2L)).thenReturn(null);

        commentService.like(1L, 2L);

        verify(commentRepository).insertLike(any());
        verify(commentRepository).incrementLikeCount(1L);
    }

    @Test
    void shouldNotDoubleLike() {
        when(commentRepository.findById(1L)).thenReturn(comment);
        when(commentRepository.findLike(1L, 2L)).thenReturn(new CommentLike());

        commentService.like(1L, 2L);

        verify(commentRepository, never()).insertLike(any());
        verify(commentRepository, never()).incrementLikeCount(anyLong());
    }

    @Test
    void shouldUnlikeComment() {
        when(commentRepository.findById(1L)).thenReturn(comment);
        when(commentRepository.findLike(1L, 2L)).thenReturn(new CommentLike());

        commentService.unlike(1L, 2L);

        verify(commentRepository).deleteLike(1L, 2L);
        verify(commentRepository).decrementLikeCount(1L);
    }

    @Test
    void shouldUnlikeWhenNotLiked() {
        when(commentRepository.findById(1L)).thenReturn(comment);
        when(commentRepository.findLike(1L, 2L)).thenReturn(null);

        commentService.unlike(1L, 2L);

        verify(commentRepository, never()).deleteLike(anyLong(), anyLong());
    }

    @Test
    void shouldDeleteComment() {
        when(commentRepository.findById(1L)).thenReturn(comment);

        commentService.delete(1L, 1L);

        verify(commentRepository).softDelete(1L);
    }

    @Test
    void shouldThrowWhenNonAuthorDeletes() {
        when(commentRepository.findById(1L)).thenReturn(comment);

        assertThatThrownBy(() -> commentService.delete(1L, 2L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    void shouldThrowWhenCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(null);

        assertThatThrownBy(() -> commentService.like(999L, 1L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }
}
