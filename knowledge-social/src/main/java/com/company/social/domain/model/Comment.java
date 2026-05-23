package com.company.social.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {
    private Long contentId;
    private Long parentId;
    private Long replyToId;
    private Long replyToUserId;
    private Long userId;
    private String body;
    private Integer likeCount;
    private String status;
    private String auditStatus;
}
