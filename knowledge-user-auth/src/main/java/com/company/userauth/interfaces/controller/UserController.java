package com.company.userauth.interfaces.controller;

import com.company.common.result.Result;
import com.company.userauth.application.dto.CreateUserRequest;
import com.company.userauth.application.dto.UpdateRoleRequest;
import com.company.userauth.application.dto.UserVO;
import com.company.userauth.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public Result<List<UserVO>> list() {
        return Result.ok(authService.listUsers());
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody CreateUserRequest req) {
        authService.createUser(req.getUsername(), req.getPassword(),
                req.getDisplayName(), req.getEmail(), req.getDepartmentId());
        return Result.ok(null);
    }

    @PutMapping("/{userId}/role")
    public Result<Void> updateRole(@PathVariable Long userId, @Valid @RequestBody UpdateRoleRequest req) {
        authService.updateUserRole(userId, req.getRole());
        return Result.ok(null);
    }
}
