package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import com.company.content.domain.model.enums.ContentType;
import com.company.content.domain.model.enums.PublishStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_content")
public class KnowledgeContent extends BaseEntity {
    private String title;
    private String body;
    private ContentType contentType;
    private PublishStatus status;
    private Long createdBy;
    private LocalDateTime publishedAt;

    @TableLogic
    private Integer isDeleted;
}
