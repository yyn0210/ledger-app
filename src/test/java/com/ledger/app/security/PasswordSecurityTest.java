package com.ledger.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
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
 * 密码安全测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PasswordSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPasswordNotReturnedInResponse() throws Exception {
        // 注册用户
        String username = "pwd_test_" + System.currentTimeMillis();
        String password = "TestPassword123!";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password(password)
                .email("pwd_test@example.com")
                .build();

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 验证响应中不包含密码
        assert !registerResponse.contains(password);
        assert !registerResponse.contains("password");
    }

    @Test
    void testPasswordNotReturnedInUserProfile() throws Exception {
        // 注册并登录
        String username = "pwd_test2_" + System.currentTimeMillis();
        String password = "TestPassword123!";
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password(password)
                .email("pwd_test2@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AuthResponse> result = objectMapper.readValue(loginResponse, objectMapper.getTypeFactory()
                .constructParametricType(Result.class, AuthResponse.class));
        String token = result.getData().getToken();

        // 获取用户信息
        String profileResponse = mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 验证响应中不包含密码
        assert !profileResponse.contains(password);
        assert !profileResponse.toLowerCase().contains("password");
    }

    @Test
    void testPasswordComplexityRequirement() throws Exception {
        // 测试密码复杂度要求
        String[] weakPasswords = {
                "12345678",      // 只有数字
                "abcdefgh",      // 只有小写字母
                "ABCDEFGH",      // 只有大写字母
                "Test123",       // 太短
                "test123456",    // 没有大写字母
                "TEST123456",    // 没有小写字母
                "TestTest"       // 没有数字
        };

        for (String password : weakPasswords) {
            RegisterRequest request = RegisterRequest.builder()
                    .username("weak_pwd_test_" + System.currentTimeMillis())
                    .password(password)
                    .email("weak_pwd@example.com")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Test
    void testStrongPasswordAccepted() throws Exception {
        String[] strongPasswords = {
                "Test123456!",
                "Str0ngP@ssw0rd",
                "MySecure#Pass99",
                "C0mplex!Pass"
        };

        for (String password : strongPasswords) {
            RegisterRequest request = RegisterRequest.builder()
                    .username("strong_pwd_test_" + System.currentTimeMillis())
                    .password(password)
                    .email("strong_pwd@example.com")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
