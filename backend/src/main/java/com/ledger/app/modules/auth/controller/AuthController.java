package com.ledger.app.modules.auth.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.dto.AuthResponse;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return Result.success(response);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名/邮箱/手机号和密码登录")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "登出当前用户（客户端清除 Token 即可）")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，客户端清除 Token 即可
        return Result.success(null);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    @GetMapping("/me")
    public Result<AuthResponse.UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        // 从 SecurityContext 获取用户信息
        // 实际项目中需要从 JWT 解析用户 ID
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(
                Long.parseLong(userDetails != null ? userDetails.getUsername() : "0")
        );
        return Result.success(userInfo);
    }
}
