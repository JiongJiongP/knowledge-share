package com.company.social.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("group_member")
public class GroupMember extends BaseEntity {
    private Long groupId;
    private Long userId;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
}
