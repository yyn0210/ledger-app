package com.ledger.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.common.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证授权安全测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAccessProtectedResourceWithoutToken() throws Exception {
        // 未提供 token 访问受保护资源
        mockMvc.perform(get("/api/book/1"))
                .andExpect(status().isUnauthorized() // 401
                        .or(status().isForbidden())); // 403
    }

    @Test
    void testAccessProtectedResourceWithInvalidToken() throws Exception {
        // 提供无效的 token
        mockMvc.perform(get("/api/book/1")
                        .header("Authorization", "Bearer invalid_token_here"))
                .andExpect(status().isUnauthorized() // 401
                        .or(status().isForbidden())); // 403
    }

    @Test
    void testAccessProtectedResourceWithExpiredToken() throws Exception {
        // 提供过期的 token（格式正确但已过期）
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxNjAwMDAwMDAwfQ." +
                "expired_signature";

        mockMvc.perform(get("/api/book/1")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedResourceWithMalformedToken() throws Exception {
        // 提供格式错误的 token
        mockMvc.perform(get("/api/book/1")
                        .header("Authorization", "Bearer malformed.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedResourceWithoutBearerPrefix() throws Exception {
        // 提供 token 但没有 Bearer 前缀
        mockMvc.perform(get("/api/book/1")
                        .header("Authorization", "some_token_here"))
                .andExpect(status().isUnauthorized()
                        .or(status().isForbidden()));
    }

    @Test
    void testLoginWithWrongPassword() throws Exception {
        // 先注册
        String username = "auth_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("CorrectPassword123!")
                .email("auth_test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 使用错误密码登录
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password("WrongPassword456!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNotEqualTo(200)); // 登录失败
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistent_user_" + System.currentTimeMillis())
                .password("AnyPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNotEqualTo(200)); // 登录失败
    }

    @Test
    void testVerticalPrivilegeEscalation() throws Exception {
        // 注册用户 A
        String usernameA = "user_a_" + System.currentTimeMillis();
        RegisterRequest registerRequestA = RegisterRequest.builder()
                .username(usernameA)
                .password("Password123!")
                .email("user_a@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestA)))
                .andExpect(status().isOk());

        // 用户 A 登录
        LoginRequest loginRequestA = LoginRequest.builder()
                .username(usernameA)
                .password("Password123!")
                .build();

        String responseA = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AuthResponse> resultA = objectMapper.readValue(responseA, objectMapper.getTypeFactory()
                .constructParametricType(Result.class, AuthResponse.class));
        String tokenA = resultA.getData().getToken();

        // 用户 A 创建账本
        CreateBookRequest bookRequest = CreateBookRequest.builder()
                .name("用户 A 的账本")
                .icon("wallet")
                .build();

        String bookResponse = mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> bookResult = objectMapper.readValue(bookResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long bookId = bookResult.getData();

        // 注册用户 B
        String usernameB = "user_b_" + System.currentTimeMillis();
        RegisterRequest registerRequestB = RegisterRequest.builder()
                .username(usernameB)
                .password("Password123!")
                .email("user_b@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequestB)))
                .andExpect(status().isOk());

        // 用户 B 登录
        LoginRequest loginRequestB = LoginRequest.builder()
                .username(usernameB)
                .password("Password123!")
                .build();

        String responseB = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestB)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AuthResponse> resultB = objectMapper.readValue(responseB, objectMapper.getTypeFactory()
                .constructParametricType(Result.class, AuthResponse.class));
        String tokenB = resultB.getData().getToken();

        // 用户 B 尝试访问用户 A 的账本（应该失败）
        mockMvc.perform(get("/api/book/" + bookId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound() // 404
                        .or(status().isForbidden())); // 403
    }
}
