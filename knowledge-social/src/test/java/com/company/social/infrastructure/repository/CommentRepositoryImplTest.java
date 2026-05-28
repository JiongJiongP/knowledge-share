package com.company.social.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.social.domain.model.Comment;
import com.company.social.domain.model.CommentLike;
import com.company.social.domain.model.CommentMention;
import com.company.social.infrastructure.mapper.CommentLikeMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.CommentMentionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentRepositoryImplTest {

    @Mock private CommentMapper commentMapper;
    @Mock private CommentLikeMapper likeMapper;
    @Mock private CommentMentionMapper mentionMapper;
    @InjectMocks private CommentRepositoryImpl repo;

    @Test
    void shouldFindById() {
        Comment c = new Comment();
        c.setId(1L);
        when(commentMapper.selectById(1L)).thenReturn(c);

        Comment result = repo.findById(1L);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldInsertComment() {
        Comment c = new Comment();
        when(commentMapper.insert(any())).thenReturn(1);

        repo.insert(c);
        verify(commentMapper).insert(c);
    }

    @Test
    void shouldUpdateComment() {
        Comment c = new Comment();
        when(commentMapper.updateById(any())).thenReturn(1);

        repo.update(c);
        verify(commentMapper).updateById(c);
    }

    @Test
    void shouldSoftDelete() {
        when(commentMapper.updateById(any())).thenReturn(1);

        repo.softDelete(1L);
        verify(commentMapper).updateById(argThat(c -> "DELETED".equals(c.getStatus())));
    }

    @Test
    void shouldFindLike() {
        CommentLike like = new CommentLike();
        when(likeMapper.selectOne(any())).thenReturn(like);

        CommentLike result = repo.findLike(1L, 2L);
        assertThat(result).isNotNull();
    }

    @Test
    void shouldInsertLike() {
        CommentLike like = new CommentLike();
        when(likeMapper.insert(any())).thenReturn(1);

        repo.insertLike(like);
        verify(likeMapper).insert(like);
    }

    @Test
    void shouldDeleteLike() {
        when(likeMapper.delete(any())).thenReturn(1);

        repo.deleteLike(1L, 2L);
        verify(likeMapper).delete(any());
    }

    @Test
    void shouldIncrementLikeCount() {
        Comment c = new Comment();
        c.setId(1L);
        c.setLikeCount(5);
        when(commentMapper.selectById(1L)).thenReturn(c);
        when(commentMapper.updateById(any())).thenReturn(1);

        repo.incrementLikeCount(1L);
        verify(commentMapper).updateById(argThat(x -> x.getLikeCount() == 6));
    }

    @Test
    void shouldIncrementLikeCountWhenNull() {
        Comment c = new Comment();
        c.setId(1L);
        c.setLikeCount(null);
        when(commentMapper.selectById(1L)).thenReturn(c);
        when(commentMapper.updateById(any())).thenReturn(1);

        repo.incrementLikeCount(1L);
        verify(commentMapper).updateById(argThat(x -> x.getLikeCount() == 1));
    }

    @Test
    void shouldNotIncrementWhenCommentNotFound() {
        when(commentMapper.selectById(1L)).thenReturn(null);

        repo.incrementLikeCount(1L);
        verify(commentMapper, never()).updateById(any());
    }

    @Test
    void shouldDecrementLikeCount() {
        Comment c = new Comment();
        c.setId(1L);
        c.setLikeCount(5);
        when(commentMapper.selectById(1L)).thenReturn(c);
        when(commentMapper.updateById(any())).thenReturn(1);

        repo.decrementLikeCount(1L);
        verify(commentMapper).updateById(argThat(x -> x.getLikeCount() == 4));
    }

    @Test
    void shouldNotDecrementWhenCountIsZero() {
        Comment c = new Comment();
        c.setId(1L);
        c.setLikeCount(0);
        when(commentMapper.selectById(1L)).thenReturn(c);

        repo.decrementLikeCount(1L);
        verify(commentMapper, never()).updateById(any());
    }

    @Test
    void shouldInsertMention() {
        CommentMention mention = new CommentMention();
        when(mentionMapper.insert(any())).thenReturn(1);

        repo.insertMention(mention);
        verify(mentionMapper).insert(mention);
    }
}
