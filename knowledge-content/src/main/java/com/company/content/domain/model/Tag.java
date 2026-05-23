package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tag")
public class Tag extends BaseEntity {
    private String name;
    private String color;
    private Long createdBy;
}
