package com.company.social.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "评论内容不能为空")
    private String body;
    private Long parentId;
    private Long replyToId;
    private Long replyToUserId;
    private List<Long> mentionedUserIds;
}
