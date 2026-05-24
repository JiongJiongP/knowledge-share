package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "user_action_log", autoResultMap = true)
public class UserActionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String actionType;
    private String targetType;
    private Long targetId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String extraData;
    private LocalDateTime createdAt;
}
