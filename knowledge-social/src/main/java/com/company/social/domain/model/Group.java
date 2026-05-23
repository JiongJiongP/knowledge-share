package com.company.social.domain.model;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_info")
public class Group extends BaseEntity {
    private String name;
    private String description;
    private Long ownerId;
    private String visibility;
    private String status;

    @TableLogic
    private Integer isDeleted;
}
