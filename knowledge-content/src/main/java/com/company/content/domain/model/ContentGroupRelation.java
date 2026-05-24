package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_group_relation")
public class ContentGroupRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Long groupId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
