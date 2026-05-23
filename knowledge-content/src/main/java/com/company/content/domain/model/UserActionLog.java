package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_action_log")
public class UserActionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String actionType;
    private String targetType;
    private Long targetId;
    private String extraData;
    private LocalDateTime createdAt;
}
