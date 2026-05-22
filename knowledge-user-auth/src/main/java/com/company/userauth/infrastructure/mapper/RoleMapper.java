package com.company.userauth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.userauth.domain.model.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
