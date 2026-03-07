package com.project.healthassistant.modules.auth.controller;

import com.project.healthassistant.common.api.ApiResponse;
import com.project.healthassistant.common.trace.TraceIdHolder;
import com.project.healthassistant.modules.auth.application.AuthApplicationService;
import com.project.healthassistant.modules.auth.dto.LoginRequest;
import com.project.healthassistant.modules.auth.dto.LoginResponse;
import com.project.healthassistant.modules.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authApplicationService.register(request);
        return ApiResponse.success("注册成功", Map.of("userId", userId), TraceIdHolder.getTraceId());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authApplicationService.login(request), TraceIdHolder.getTraceId());
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, String>> me(Principal principal) {
        return ApiResponse.success(Map.of("username", principal.getName()), TraceIdHolder.getTraceId());
    }
}
