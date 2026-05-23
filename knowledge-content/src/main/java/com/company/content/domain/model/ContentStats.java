package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("content_stats")
public class ContentStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private Long viewCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long downloadCount;
    private LocalDate statDate;
}
