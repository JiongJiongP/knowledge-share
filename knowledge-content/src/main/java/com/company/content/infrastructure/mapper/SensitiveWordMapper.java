package com.company.content.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.content.domain.model.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
