package com.goaltracker.controller;

import com.goaltracker.auth.AuthService;
import com.goaltracker.dto.ApiResponse;
import com.goaltracker.dto.AuthResponse;
import com.goaltracker.dto.LoginRequest;
import com.goaltracker.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 這支 Controller 底下的路徑(/api/auth/**),在 AuthFilter 裡被列為白名單,
 * 不需要附帶 token 就能呼叫——原因很直觀:註冊、登入本來就是「還沒登入的人」要用的功能。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("註冊成功", authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("登入成功", authService.login(request));
    }
}
