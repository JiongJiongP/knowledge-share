package com.company.userauth.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.userauth.domain.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.code FROM role r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    String findRoleByUserId(Long userId);

    @Select("SELECT ur.user_id FROM user_role ur JOIN role r ON ur.role_id = r.id WHERE r.code = 'ADMIN'")
    List<Long> findAdminUserIds();

    @Select("SELECT ur.user_id, r.code FROM user_role ur JOIN role r ON ur.role_id = r.id")
    List<Map<String, Object>> findAllUserRoles();
}
