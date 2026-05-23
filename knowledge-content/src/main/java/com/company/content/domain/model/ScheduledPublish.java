package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scheduled_publish")
public class ScheduledPublish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private LocalDateTime scheduledAt;
    private String status;
    private LocalDateTime createdAt;
}
