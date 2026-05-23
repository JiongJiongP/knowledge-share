package com.company.social.domain.repository;

import com.company.social.domain.model.Comment;
import com.company.social.domain.model.CommentLike;
import com.company.social.domain.model.CommentMention;

import java.util.List;

public interface CommentRepository {
    Comment findById(Long id);
    List<Comment> findByContentId(Long contentId, Long parentId);
    void insert(Comment comment);
    void update(Comment comment);
    void softDelete(Long id);

    CommentLike findLike(Long commentId, Long userId);
    void insertLike(CommentLike like);
    void deleteLike(Long commentId, Long userId);
    void incrementLikeCount(Long commentId);
    void decrementLikeCount(Long commentId);

    void insertMention(CommentMention mention);
}
