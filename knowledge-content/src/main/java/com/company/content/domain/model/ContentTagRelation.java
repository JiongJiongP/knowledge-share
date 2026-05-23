package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_tag_relation")
public class ContentTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Long tagId;
    private LocalDateTime createdAt;
}
