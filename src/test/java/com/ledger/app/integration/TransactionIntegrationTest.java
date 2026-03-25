package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.account.dto.response.CreateAccountRequest;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 交易模块集成测试（端到端流程）
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long bookId;
    private Long categoryId;
    private Long accountId;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "tx_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("tx_test@example.com")
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

        // 创建账本
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
        bookId = bookResult.getData();

        // 创建分类
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("餐饮")
                .type(TransactionType.EXPENSE.getCode())
                .icon("food")
                .build();

        String categoryResponse = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> categoryResult = objectMapper.readValue(categoryResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        categoryId = categoryResult.getData();

        // 创建账户
        CreateAccountRequest accountRequest = CreateAccountRequest.builder()
                .name("测试银行卡")
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
        accountId = accountResult.getData();
    }

    @Test
    void testCreateTransaction() throws Exception {
        // 创建交易
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .note("测试交易")
                .build();

        String response = mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> result = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long txId = result.getData();

        assert txId != null && txId > 0;
    }

    @Test
    void testCreateTransaction_UpdatesAccountBalance() throws Exception {
        // 获取初始余额
        String accountResponse = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<com.ledger.app.modules.account.dto.response.AccountResponse> accountResult =
                objectMapper.readValue(accountResponse, objectMapper.getTypeFactory()
                        .constructParametricType(Result.class, com.ledger.app.modules.account.dto.response.AccountResponse.class));
        BigDecimal initialBalance = accountResult.getData().getBalance();

        // 创建支出交易
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk());

        // 验证余额减少
        String updatedAccountResponse = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<com.ledger.app.modules.account.dto.response.AccountResponse> updatedResult =
                objectMapper.readValue(updatedAccountResponse, objectMapper.getTypeFactory()
                        .constructParametricType(Result.class, com.ledger.app.modules.account.dto.response.AccountResponse.class));
        BigDecimal updatedBalance = updatedResult.getData().getBalance();

        // 验证余额减少了 100
        assert initialBalance.subtract(updatedBalance).compareTo(new BigDecimal("100.00")) == 0;
    }

    @Test
    void testGetTransactionsByBookId() throws Exception {
        // 获取交易列表
        mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }
}
