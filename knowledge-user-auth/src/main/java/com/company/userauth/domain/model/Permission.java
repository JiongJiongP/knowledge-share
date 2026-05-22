package com.company.userauth.domain.model;

import com.company.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission")
public class Permission extends BaseEntity {
    private String code;
    private String name;
}
