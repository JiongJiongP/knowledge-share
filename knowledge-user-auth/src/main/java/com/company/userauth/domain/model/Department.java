package com.company.userauth.domain.model;

import com.company.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {
    private String name;
    private Long parentId;
    private Integer sortOrder;
}
