package com.company.content.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data
@TableName("search_hot_keyword")
public class SearchHotKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer searchCount;
    private LocalDate statDate;
}
