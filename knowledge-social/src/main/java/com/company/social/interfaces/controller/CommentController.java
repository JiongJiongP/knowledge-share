package com.company.social.interfaces.controller;

import com.company.common.result.Result;
import com.company.social.application.dto.CreateCommentRequest;
import com.company.social.application.service.CommentService;
import com.company.social.domain.model.Comment;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/contents/{contentId}/comments")
    public Result<List<Comment>> list(@PathVariable Long contentId) {
        return Result.ok(commentService.listByContentId(contentId));
    }

    @GetMapping("/api/comments/{commentId}/replies")
    public Result<List<Comment>> replies(@PathVariable Long commentId,
                                         @RequestParam Long contentId) {
        return Result.ok(commentService.listReplies(contentId, commentId));
    }

    @PostMapping("/api/contents/{contentId}/comments")
    public Result<Comment> create(@PathVariable Long contentId,
                                  @Valid @RequestBody CreateCommentRequest req,
                                  Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Comment comment = commentService.create(contentId, userId, req.getBody(),
                req.getParentId(), req.getReplyToId(), req.getReplyToUserId(),
                req.getMentionedUserIds());
        return Result.ok(comment);
    }

    @PostMapping("/api/comments/{id}/like")
    public Result<Void> like(@PathVariable Long id, Authentication auth) {
        commentService.like(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @DeleteMapping("/api/comments/{id}/like")
    public Result<Void> unlike(@PathVariable Long id, Authentication auth) {
        commentService.unlike(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }

    @DeleteMapping("/api/comments/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        commentService.delete(id, (Long) auth.getPrincipal());
        return Result.ok(null);
    }
}
