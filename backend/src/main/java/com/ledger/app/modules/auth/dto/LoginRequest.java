package com.ledger.app.modules.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求 DTO
 */
@Data
public class LoginRequest {

    /**
     * 用户名/邮箱/手机号
     */
    @NotBlank(message = "账号不能为空")
    private String account;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 是否记住我（延长 Token 有效期）
     */
    private Boolean rememberMe = false;
}
