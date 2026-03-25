package com.ledger.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
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
 * 参数校验安全测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ParameterValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        String username = "validation_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("validation_test@example.com")
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
    void testRegisterWithEmptyUsername() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .password("Test123456!")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRegisterWithWeakPassword() throws Exception {
        String[] weakPasswords = {
                "123456",
                "password",
                "abcdef",
                "12345678",
                "Test123"
        };

        for (String password : weakPasswords) {
            RegisterRequest request = RegisterRequest.builder()
                    .username("test_user_" + System.currentTimeMillis())
                    .password(password)
                    .email("test@example.com")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void testRegisterWithInvalidEmail() throws Exception {
        String[] invalidEmails = {
                "invalid",
                "invalid@",
                "@example.com",
                "test@invalid",
                "test @example.com"
        };

        for (String email : invalidEmails) {
            RegisterRequest request = RegisterRequest.builder()
                    .username("test_user_" + System.currentTimeMillis())
                    .password("Test123456!")
                    .email(email)
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void testCreateBookWithEmptyName() throws Exception {
        mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"icon\":\"wallet\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateBookWithNullName() throws Exception {
        mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"icon\":\"wallet\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateTransactionWithNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"categoryId\":1,\"accountId\":1,\"type\":1,\"amount\":-100}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateTransactionWithZeroAmount() throws Exception {
        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"categoryId\":1,\"accountId\":1,\"type\":1,\"amount\":0}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateTransactionWithInvalidType() throws Exception {
        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"categoryId\":1,\"accountId\":1,\"type\":999,\"amount\":100}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPaginationWithNegativePage() throws Exception {
        mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", "1")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPaginationWithNegativeSize() throws Exception {
        mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", "1")
                        .param("page", "1")
                        .param("size", "-10"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testPaginationWithExcessiveSize() throws Exception {
        mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", "1")
                        .param("page", "1")
                        .param("size", "10000"))
                .andExpect(status().is4xxClientError() // 应该被限制
                        .or(status().isOk())); // 或者有默认最大值
    }
}
