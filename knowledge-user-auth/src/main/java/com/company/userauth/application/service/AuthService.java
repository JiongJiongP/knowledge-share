package com.company.userauth.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.exception.BizException;
import com.company.userauth.domain.model.User;
import com.company.userauth.infrastructure.mapper.RoleMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            throw new BizException(401, "用户名或密码错误");
        }
        if ("DISABLED".equals(user.getStatus())) {
            throw new BizException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String role = roleMapper.findRoleByUserId(user.getId());
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        return jwtUtil.generate(user.getId(), user.getUsername(), role);
    }

    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户");
        }
        user.setPassword(null);
        return user;
    }
}
