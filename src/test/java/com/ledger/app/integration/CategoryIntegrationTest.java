package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
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
 * 分类模块集成测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long categoryId;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "category_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("category_test@example.com")
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

        // 创建分类
        CreateCategoryRequest createRequest = CreateCategoryRequest.builder()
                .name("测试分类")
                .type(TransactionType.EXPENSE.getCode())
                .icon("test")
                .build();

        String createResponse = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> createResult = objectMapper.readValue(createResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        categoryId = createResult.getData();
    }

    @Test
    void testCreateCategory() throws Exception {
        assert categoryId != null && categoryId > 0;
    }

    @Test
    void testGetCategoriesByType() throws Exception {
        mockMvc.perform(get("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("type", String.valueOf(TransactionType.EXPENSE.getCode())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdateCategory() throws Exception {
        // 更新分类
        CreateCategoryRequest updateRequest = CreateCategoryRequest.builder()
                .name("更新后的分类")
                .type(TransactionType.EXPENSE.getCode())
                .icon("updated")
                .build();

        mockMvc.perform(put("/api/category/" + categoryId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteCategory() throws Exception {
        // 删除分类
        mockMvc.perform(delete("/api/category/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteCategory_WithTransactionReference_ShouldFail() throws Exception {
        // 1. 创建账本
        CreateBookRequest bookRequest = CreateBookRequest.builder()
                .name("测试账本")
                .icon("wallet")
                .build();

        String bookResponse = mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> bookResult = objectMapper.readValue(bookResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long bookId = bookResult.getData();

        // 2. 创建账户
        CreateAccountRequest accountRequest = CreateAccountRequest.builder()
                .name("测试账户")
                .type(1)
                .balance(new BigDecimal("10000.00"))
                .bookId(bookId)
                .build();

        String accountResponse = mockMvc.perform(post("/api/account")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> accountResult = objectMapper.readValue(accountResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long accountId = accountResult.getData();

        // 3. 创建使用该分类的交易
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .note("测试交易")
                .build();

        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk());

        // 4. 尝试删除被引用的分类 - 应该失败
        mockMvc.perform(delete("/api/category/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
