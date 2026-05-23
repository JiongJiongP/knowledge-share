package com.company.social.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.social.domain.model.CommentMention;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMentionMapper extends BaseMapper<CommentMention> {
}
