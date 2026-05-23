package com.company.social.application.service;

import com.company.common.exception.BizException;
import com.company.social.domain.model.Comment;
import com.company.social.domain.model.CommentLike;
import com.company.social.domain.model.CommentMention;
import com.company.social.domain.repository.CommentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<Comment> listByContentId(Long contentId) {
        List<Comment> parents = commentRepository.findByContentId(contentId, null);
        for (Comment parent : parents) {
            List<Comment> replies = commentRepository.findByContentId(contentId, parent.getId());
            // replies are only one level deep
        }
        return parents;
    }

    public List<Comment> listReplies(Long contentId, Long parentId) {
        return commentRepository.findByContentId(contentId, parentId);
    }

    @Transactional
    public Comment create(Long contentId, Long userId, String body,
                          Long parentId, Long replyToId, Long replyToUserId,
                          List<Long> mentionedUserIds) {
        Comment comment = new Comment();
        comment.setContentId(contentId);
        comment.setUserId(userId);
        comment.setBody(body);
        comment.setParentId(parentId);
        comment.setReplyToId(replyToId);
        comment.setReplyToUserId(replyToUserId);
        comment.setLikeCount(0);
        comment.setStatus("PUBLISHED");
        comment.setAuditStatus("APPROVED");
        commentRepository.insert(comment);

        if (mentionedUserIds != null) {
            for (Long mentionedId : mentionedUserIds) {
                CommentMention mention = new CommentMention();
                mention.setCommentId(comment.getId());
                mention.setMentionedUserId(mentionedId);
                commentRepository.insertMention(mention);
            }
        }

        return comment;
    }

    @Transactional
    public void like(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) throw BizException.notFound("评论");

        CommentLike existing = commentRepository.findLike(commentId, userId);
        if (existing != null) return; // already liked

        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        try {
            commentRepository.insertLike(like);
            commentRepository.incrementLikeCount(commentId);
        } catch (DataIntegrityViolationException e) {
            // concurrent duplicate like, ignore
        }
    }

    @Transactional
    public void unlike(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) throw BizException.notFound("评论");

        CommentLike existing = commentRepository.findLike(commentId, userId);
        if (existing == null) return;

        commentRepository.deleteLike(commentId, userId);
        commentRepository.decrementLikeCount(commentId);
    }

    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) throw BizException.notFound("评论");
        if (!comment.getUserId().equals(userId)) {
            throw BizException.forbidden();
        }
        commentRepository.softDelete(commentId);
    }
}
