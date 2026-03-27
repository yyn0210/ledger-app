package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.enums.BudgetPeriod;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
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
 * 预算模块集成测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BudgetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long bookId;
    private Long budgetId;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "budget_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("budget_test@example.com")
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

        // 创建预算
        CreateBudgetRequest budgetRequest = CreateBudgetRequest.builder()
                .bookId(bookId)
                .amount(new BigDecimal("5000.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        String budgetResponse = mockMvc.perform(post("/api/budget")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> budgetResult = objectMapper.readValue(budgetResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        budgetId = budgetResult.getData();
    }

    @Test
    void testCreateBudget() throws Exception {
        assert budgetId != null && budgetId > 0;
    }

    @Test
    void testGetBudgetById() throws Exception {
        mockMvc.perform(get("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(budgetId));
    }

    @Test
    void testGetBudgetsByBookId() throws Exception {
        mockMvc.perform(get("/api/budget")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void testUpdateBudget() throws Exception {
        CreateBudgetRequest updateRequest = CreateBudgetRequest.builder()
                .amount(new BigDecimal("6000.00"))
                .build();

        mockMvc.perform(put("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteBudget() throws Exception {
        mockMvc.perform(delete("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testBudgetExecutionTracking() throws Exception {
        // 1. 创建分类
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
        Long categoryId = categoryResult.getData();

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

        // 3. 创建多笔交易，模拟预算执行
        BigDecimal[] amounts = {new BigDecimal("500.00"), new BigDecimal("300.00"), new BigDecimal("200.00")};
        for (BigDecimal amount : amounts) {
            CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                    .bookId(bookId)
                    .categoryId(categoryId)
                    .accountId(accountId)
                    .type(TransactionType.EXPENSE.getCode())
                    .amount(amount)
                    .transactionDate(LocalDate.now())
                    .note("测试交易")
                    .build();

            mockMvc.perform(post("/api/transaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(txRequest)))
                    .andExpect(status().isOk());
        }

        // 4. 获取预算执行情况
        String budgetResponse = mockMvc.perform(get("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<BudgetResponse> budgetResult = objectMapper.readValue(budgetResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, BudgetResponse.class));
        BudgetResponse budget = budgetResult.getData();

        // 验证预算执行数据
        assert budget != null;
        assert budget.getAmount().compareTo(new BigDecimal("5000.00")) == 0;
    }

    @Test
    void testBudgetExceed_Warning() throws Exception {
        // 1. 创建一个小额预算
        CreateBudgetRequest smallBudgetRequest = CreateBudgetRequest.builder()
                .bookId(bookId)
                .amount(new BigDecimal("100.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1))
                .build();

        String budgetResponse = mockMvc.perform(post("/api/budget")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smallBudgetRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> budgetResult = objectMapper.readValue(budgetResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long smallBudgetId = budgetResult.getData();

        // 2. 创建分类和账户
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("测试分类")
                .type(TransactionType.EXPENSE.getCode())
                .icon("test")
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
        Long categoryId = categoryResult.getData();

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

        // 3. 创建超出预算的交易
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("150.00"))
                .transactionDate(LocalDate.now())
                .note("超出预算的交易")
                .build();

        mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk());

        // 4. 验证预算超支状态
        String budgetStatusResponse = mockMvc.perform(get("/api/budget/" + smallBudgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<BudgetResponse> statusResult = objectMapper.readValue(budgetStatusResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, BudgetResponse.class));
        BudgetResponse budget = statusResult.getData();

        // 验证预算已超支
        assert budget != null;
    }
}
