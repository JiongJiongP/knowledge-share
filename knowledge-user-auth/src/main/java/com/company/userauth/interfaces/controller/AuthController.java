package com.company.userauth.interfaces.controller;

import com.company.common.result.Result;
import com.company.userauth.application.dto.LoginRequest;
import com.company.userauth.application.dto.LoginResponse;
import com.company.userauth.application.service.AuthService;
import com.company.userauth.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        return Result.ok(new LoginResponse(token));
    }

    @GetMapping("/me")
    public Result<User> me(Authentication auth) {
        if (auth == null) {
            return Result.fail(401, "未认证");
        }
        Long userId = (Long) auth.getPrincipal();
        return Result.ok(authService.getCurrentUser(userId));
    }
}
