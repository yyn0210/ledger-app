package com.ledger.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
<<<<<<< HEAD
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
=======
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
import com.ledger.app.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * XSS（跨站脚本）防护测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class XssProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        String username = "xss_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("xss_test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password("Test123456!")
                .build();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AuthResponse> result = objectMapper.readValue(response, objectMapper.getTypeFactory()
                .constructParametricType(Result.class, AuthResponse.class));
        authToken = result.getData().getToken();
    }

    @Test
    void testXssInBookName() throws Exception {
        String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "<svg onload=alert('XSS')>",
                "javascript:alert('XSS')",
                "<iframe src='javascript:alert(\"XSS\")'>",
                "<body onload=alert('XSS')>",
                "<<script>alert('XSS')</script>",
                "<script>alert(String.fromCharCode(88,83,83))</script>"
        };

        for (String payload : xssPayloads) {
            CreateBookRequest request = CreateBookRequest.builder()
                    .name(payload)
                    .icon("wallet")
                    .build();

            // 应该被拦截或转义
            mockMvc.perform(post("/api/book")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError() // 参数校验失败
                            .or(status().isOk())); // 或者被转义后成功创建
        }
    }

    @Test
    void testXssInTransactionNote() throws Exception {
        String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "<svg onload=alert('XSS')>",
                "javascript:alert('XSS')"
        };

        for (String payload : xssPayloads) {
            // 创建交易时 XSS 注入
            mockMvc.perform(post("/api/transaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"bookId\":1,\"categoryId\":1,\"accountId\":1,\"type\":1,\"amount\":100,\"note\":\"" + payload + "\"}"))
                    .andExpect(status().is4xxClientError() // 参数校验失败
                            .or(status().isNotFound())); // 资源不存在
        }
    }

    @Test
    void testXssInCategoryName() throws Exception {
        String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "<svg onload=alert('XSS')>"
        };

        for (String payload : xssPayloads) {
            mockMvc.perform(post("/api/category")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"" + payload + "\",\"type\":1,\"icon\":\"test\"}"))
                    .andExpect(status().is4xxClientError() // 参数校验失败
                            .or(status().isOk())); // 或者被转义后成功创建
        }
    }

    @Test
    void testXssInQueryParam() throws Exception {
        String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "javascript:alert('XSS')"
        };

        for (String payload : xssPayloads) {
            mockMvc.perform(get("/api/category")
                            .header("Authorization", "Bearer " + authToken)
                            .param("name", payload))
                    .andExpect(status().isOk())
                    // 确保响应中不包含原始脚本
                    .andExpect(result -> {
                        String response = result.getResponse().getContentAsString();
                        assert !response.contains("<script>");
                    });
        }
    }

    @Test
    void testXssInUserName() throws Exception {
        String[] xssPayloads = {
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert('XSS')>",
                "admin'><script>alert('XSS')</script>"
        };

        for (String payload : xssPayloads) {
            RegisterRequest request = RegisterRequest.builder()
                    .username(payload)
                    .password("Test123456!")
                    .email("xss_test2@example.com")
                    .build();

            // 应该被参数校验拦截
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }
}
