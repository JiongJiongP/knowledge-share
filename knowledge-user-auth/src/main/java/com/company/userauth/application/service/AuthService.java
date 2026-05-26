package com.company.userauth.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.common.exception.BizException;
import com.company.userauth.application.dto.UserVO;
import com.company.userauth.domain.model.Department;
import com.company.userauth.domain.model.Role;
import com.company.userauth.domain.model.User;
import com.company.userauth.domain.model.UserRole;
import com.company.userauth.infrastructure.mapper.DepartmentMapper;
import com.company.userauth.infrastructure.mapper.RoleMapper;
import com.company.userauth.infrastructure.mapper.UserMapper;
import com.company.userauth.infrastructure.mapper.UserRoleMapper;
import com.company.userauth.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final DepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper,
                       UserRoleMapper userRoleMapper, DepartmentMapper departmentMapper,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.departmentMapper = departmentMapper;
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
        String role = roleMapper.findRoleByUserId(userId);
        user.setRoles(role != null ? List.of(role) : List.of("USER"));
        return user;
    }

    public List<UserVO> listUsers() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, String> deptMap = departmentMapper.selectList(new LambdaQueryWrapper<>()).stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        Map<Long, String> roleMap = roleMapper.findAllUserRoles().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row.get("user_id"),
                        row -> (String) row.get("code"),
                        (a, b) -> a));

        return users.stream().map(u -> {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setDisplayName(u.getDisplayName());
            vo.setEmail(u.getEmail());
            vo.setDepartmentId(u.getDepartmentId());
            vo.setDepartmentName(deptMap.getOrDefault(u.getDepartmentId(), "-"));
            String roleCode = roleMap.getOrDefault(u.getId(), "USER");
            vo.setRoleName("ADMIN".equals(roleCode) ? "系统管理员" : "普通用户");
            vo.setStatus(u.getStatus());
            vo.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void createUser(String username, String password, String displayName, String email, Long departmentId) {
        User exist = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exist != null) {
            throw new BizException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setDisplayName(displayName != null ? displayName : username);
        user.setEmail(email);
        user.setDepartmentId(departmentId);
        user.setStatus("ACTIVE");
        userMapper.insert(user);

        Role userRole = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getCode, "USER"));
        if (userRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(userRole.getId());
            userRoleMapper.insert(ur);
        }
    }

    @Transactional
    public void updateUserRole(Long userId, String roleCode) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BizException.notFound("用户");
        }
        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode));
        if (role == null) {
            throw new BizException(400, "角色不存在");
        }
        userRoleMapper.delete(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);
    }
}
