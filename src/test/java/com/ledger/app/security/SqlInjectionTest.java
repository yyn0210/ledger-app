package com.ledger.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
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
 * SQL 注入防护测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SqlInjectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录获取 token
        String username = "security_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("security_test@example.com")
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
    void testSqlInjectionInLogin_Username() throws Exception {
        // SQL 注入攻击载荷
        String[] injectionPayloads = {
                "' OR '1'='1",
                "admin' --",
                "admin' /*",
                "admin' OR '1'='1' --",
                "admin' UNION SELECT * FROM users --",
                "'; DROP TABLE users; --",
                "1; DELETE FROM users",
                "' UNION SELECT null, password FROM users --"
        };

        for (String payload : injectionPayloads) {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username(payload)
                    .password("any_password")
                    .build();

            // 应该返回认证失败，而不是 SQL 错误或泄露数据
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNumber())
                    // 确保不返回数据库错误信息
                    .andExpect(result -> {
                        String response = result.getResponse().getContentAsString();
                        assert !response.contains("SQL");
                        assert !response.contains("database");
                        assert !response.contains("PostgreSQL");
                    });
        }
    }

    @Test
    void testSqlInjectionInLogin_Password() throws Exception {
        String[] injectionPayloads = {
                "' OR '1'='1",
                "password' --",
                "' UNION SELECT null, password FROM users --"
        };

        for (String payload : injectionPayloads) {
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("nonexistent_user")
                    .password(payload)
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isNumber());
        }
    }

    @Test
    void testSqlInjectionInBookId() throws Exception {
        String[] injectionPayloads = {
                "1 OR 1=1",
                "1; DROP TABLE book",
                "1 UNION SELECT * FROM users",
                "1' OR '1'='1",
                "1' UNION SELECT null, null, null FROM book --"
        };

        for (String payload : injectionPayloads) {
            // 应该返回 404 或错误，而不是执行 SQL 注入
            mockMvc.perform(get("/api/book/" + payload)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void testSqlInjectionInQueryParams() throws Exception {
        String[] injectionPayloads = {
                "1 OR 1=1",
                "1; DROP TABLE transaction",
                "1' OR '1'='1"
        };

        for (String payload : injectionPayloads) {
            mockMvc.perform(get("/api/transaction")
                            .header("Authorization", "Bearer " + authToken)
                            .param("bookId", payload)
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void testSqlInjectionInCategoryName() throws Exception {
        String injectionPayload = "' OR '1'='1";

        mockMvc.perform(get("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("name", injectionPayload))
                .andExpect(status().isOk())
                // 确保返回的是空列表或正常数据，而不是数据库错误
                .andExpect(jsonPath("$.data").isArray());
    }
}
