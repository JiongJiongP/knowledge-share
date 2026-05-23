package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("content_version")
public class ContentVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Integer versionNumber;
    private String title;
    private String body;
    private String changeSummary;
    private Long createdBy;
    private LocalDateTime createdAt;
}
