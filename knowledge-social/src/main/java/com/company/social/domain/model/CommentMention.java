package com.company.social.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("comment_mention")
public class CommentMention {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long mentionedUserId;
}
