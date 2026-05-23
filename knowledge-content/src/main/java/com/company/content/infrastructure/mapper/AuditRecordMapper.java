package com.company.content.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.content.domain.model.AuditRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {
}
