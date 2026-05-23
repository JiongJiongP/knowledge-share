package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_template")
public class ContentTemplate extends BaseEntity {
    private String name;
    private String description;
    private String contentType;
    private String body;
    private Integer isSystem;
    private Long createdBy;
}
