package com.company.social.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.social.domain.model.Comment;
import com.company.social.domain.model.CommentLike;
import com.company.social.domain.model.CommentMention;
import com.company.social.domain.repository.CommentRepository;
import com.company.social.infrastructure.mapper.CommentLikeMapper;
import com.company.social.infrastructure.mapper.CommentMapper;
import com.company.social.infrastructure.mapper.CommentMentionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper likeMapper;
    private final CommentMentionMapper mentionMapper;

    public CommentRepositoryImpl(CommentMapper commentMapper, CommentLikeMapper likeMapper,
                                  CommentMentionMapper mentionMapper) {
        this.commentMapper = commentMapper;
        this.likeMapper = likeMapper;
        this.mentionMapper = mentionMapper;
    }

    @Override
    public Comment findById(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public List<Comment> findByContentId(Long contentId, Long parentId) {
        LambdaQueryWrapper<Comment> qw = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getContentId, contentId)
                .eq(Comment::getStatus, "PUBLISHED");
        if (parentId == null) {
            qw.isNull(Comment::getParentId);
        } else {
            qw.eq(Comment::getParentId, parentId);
        }
        qw.orderByAsc(Comment::getCreatedAt);
        return commentMapper.selectList(qw);
    }

    @Override
    public void insert(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public void update(Comment comment) {
        commentMapper.updateById(comment);
    }

    @Override
    public void softDelete(Long id) {
        Comment c = new Comment();
        c.setId(id);
        c.setStatus("DELETED");
        commentMapper.updateById(c);
    }

    @Override
    public CommentLike findLike(Long commentId, Long userId) {
        return likeMapper.selectOne(
            new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
        );
    }

    @Override
    public void insertLike(CommentLike like) {
        likeMapper.insert(like);
    }

    @Override
    public void deleteLike(Long commentId, Long userId) {
        likeMapper.delete(
            new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId)
        );
    }

    @Override
    public void incrementLikeCount(Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c != null) {
            c.setLikeCount(c.getLikeCount() != null ? c.getLikeCount() + 1 : 1);
            commentMapper.updateById(c);
        }
    }

    @Override
    public void decrementLikeCount(Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c != null && c.getLikeCount() != null && c.getLikeCount() > 0) {
            c.setLikeCount(c.getLikeCount() - 1);
            commentMapper.updateById(c);
        }
    }

    @Override
    public void insertMention(CommentMention mention) {
        mentionMapper.insert(mention);
    }
}
