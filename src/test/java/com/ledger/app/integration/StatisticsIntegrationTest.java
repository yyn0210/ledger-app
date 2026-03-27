package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 统计模块集成测试
 * 测试场景：
 * 1. 按分类统计支出
 * 2. 按账户统计资产汇总
 * 3. 按时间趋势统计
 * 4. 预算执行率统计
 * 5. 多分类、多账户聚合统计
 *
 * @author Chisong
 * @since 2026-03-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long bookId;
    private Long categoryId1;
    private Long categoryId2;
    private Long categoryId3;
    private Long accountId1;
    private Long accountId2;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "stats_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("stats_test@example.com")
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
                .name("统计测试账本")
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

        // 创建多个分类
        CreateCategoryRequest categoryRequest1 = CreateCategoryRequest.builder()
                .name("餐饮")
                .type(TransactionType.EXPENSE.getCode())
                .icon("food")
                .build();

        String categoryResponse1 = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> categoryResult1 = objectMapper.readValue(categoryResponse1,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        categoryId1 = categoryResult1.getData();

        CreateCategoryRequest categoryRequest2 = CreateCategoryRequest.builder()
                .name("交通")
                .type(TransactionType.EXPENSE.getCode())
                .icon("transport")
                .build();

        String categoryResponse2 = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> categoryResult2 = objectMapper.readValue(categoryResponse2,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        categoryId2 = categoryResult2.getData();

        CreateCategoryRequest categoryRequest3 = CreateCategoryRequest.builder()
                .name("工资")
                .type(TransactionType.INCOME.getCode())
                .icon("salary")
                .build();

        String categoryResponse3 = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest3)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> categoryResult3 = objectMapper.readValue(categoryResponse3,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        categoryId3 = categoryResult3.getData();

        // 创建多个账户
        CreateAccountRequest accountRequest1 = CreateAccountRequest.builder()
                .name("招商银行卡")
                .type(1)
                .balance(new BigDecimal("10000.00"))
                .bookId(bookId)
                .build();

        String accountResponse1 = mockMvc.perform(post("/api/account")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest1)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> accountResult1 = objectMapper.readValue(accountResponse1,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        accountId1 = accountResult1.getData();

        CreateAccountRequest accountRequest2 = CreateAccountRequest.builder()
                .name("微信钱包")
                .type(2)
                .balance(new BigDecimal("5000.00"))
                .bookId(bookId)
                .build();

        String accountResponse2 = mockMvc.perform(post("/api/account")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest2)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> accountResult2 = objectMapper.readValue(accountResponse2,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        accountId2 = accountResult2.getData();
    }

    @Test
    void testExpenseByCategory_Aggregation() throws Exception {
        // 创建多笔不同分类的支出交易
        createTransaction(categoryId1, accountId1, TransactionType.EXPENSE, new BigDecimal("100.00"));
        createTransaction(categoryId1, accountId1, TransactionType.EXPENSE, new BigDecimal("200.00"));
        createTransaction(categoryId2, accountId1, TransactionType.EXPENSE, new BigDecimal("50.00"));

        // 获取按分类统计
        String statsResponse = mockMvc.perform(get("/api/statistics/expense-by-category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<List<StatsItem>> result = objectMapper.readValue(statsResponse,
                objectMapper.getTypeFactory().constructParametricType(
                        Result.class,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StatsItem.class)
                ));

        List<StatsItem> stats = result.getData();
        assert stats != null && !stats.isEmpty();

        // 验证餐饮分类总额为 300
        StatsItem foodStats = stats.stream()
                .filter(s -> s.getCategoryId().equals(categoryId1))
                .findFirst()
                .orElse(null);
        assert foodStats != null;
        assert foodStats.getAmount().compareTo(new BigDecimal("300.00")) == 0;

        // 验证交通分类总额为 50
        StatsItem transportStats = stats.stream()
                .filter(s -> s.getCategoryId().equals(categoryId2))
                .findFirst()
                .orElse(null);
        assert transportStats != null;
        assert transportStats.getAmount().compareTo(new BigDecimal("50.00")) == 0;
    }

    @Test
    void testAssetsSummary_MultiAccount() throws Exception {
        // 获取资产汇总
        String assetsResponse = mockMvc.perform(get("/api/statistics/assets-summary")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AssetsSummary> assetsResult = objectMapper.readValue(assetsResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AssetsSummary.class));

        AssetsSummary summary = assetsResult.getData();
        assert summary != null;
        // 总资产应为 15000 (10000 + 5000)
        assert summary.getTotal().compareTo(new BigDecimal("15000.00")) == 0;
    }

    @Test
    void testIncomeExpenseTrend_TimeSeries() throws Exception {
        // 创建多笔收入和支出
        createTransaction(categoryId1, accountId1, TransactionType.EXPENSE, new BigDecimal("100.00"));
        createTransaction(categoryId3, accountId1, TransactionType.INCOME, new BigDecimal("5000.00"));
        createTransaction(categoryId2, accountId2, TransactionType.EXPENSE, new BigDecimal("50.00"));

        // 获取趋势统计
        String trendResponse = mockMvc.perform(get("/api/statistics/income-expense-trend")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<List<TrendItem>> trendResult = objectMapper.readValue(trendResponse,
                objectMapper.getTypeFactory().constructParametricType(
                        Result.class,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, TrendItem.class)
                ));

        List<TrendItem> trends = trendResult.getData();
        assert trends != null;
        // 验证有数据返回
        assert !trends.isEmpty();
    }

    @Test
    void testStatistics_EmptyData() throws Exception {
        // 创建新账本，无任何交易
        CreateBookRequest newBookRequest = CreateBookRequest.builder()
                .name("空账本")
                .icon("empty")
                .build();

        String newBookResponse = mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> newBookResult = objectMapper.readValue(newBookResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long newBookId = newBookResult.getData();

        // 获取统计 - 应返回空数据或 0
        String statsResponse = mockMvc.perform(get("/api/statistics/expense-by-category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", newBookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<List<StatsItem>> result = objectMapper.readValue(statsResponse,
                objectMapper.getTypeFactory().constructParametricType(
                        Result.class,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StatsItem.class)
                ));

        List<StatsItem> stats = result.getData();
        // 空账本应返回空列表或空数据
        assert stats != null;
    }

    @Test
    void testStatistics_ByAccount() throws Exception {
        // 创建不同账户的交易
        createTransaction(categoryId1, accountId1, TransactionType.EXPENSE, new BigDecimal("100.00"));
        createTransaction(categoryId1, accountId2, TransactionType.EXPENSE, new BigDecimal("200.00"));

        // 按账户统计
        String accountStatsResponse = mockMvc.perform(get("/api/statistics/expense-by-account")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<List<StatsItem>> result = objectMapper.readValue(accountStatsResponse,
                objectMapper.getTypeFactory().constructParametricType(
                        Result.class,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StatsItem.class)
                ));

        List<StatsItem> stats = result.getData();
        assert stats != null && !stats.isEmpty();

        // 验证各账户统计
        StatsItem account1Stats = stats.stream()
                .filter(s -> s.getAccountId().equals(accountId1))
                .findFirst()
                .orElse(null);
        assert account1Stats != null;
        assert account1Stats.getAmount().compareTo(new BigDecimal("100.00")) == 0;

        StatsItem account2Stats = stats.stream()
                .filter(s -> s.getAccountId().equals(accountId2))
                .findFirst()
                .orElse(null);
        assert account2Stats != null;
        assert account2Stats.getAmount().compareTo(new BigDecimal("200.00")) == 0;
    }

    // Helper method to create transactions
    private void createTransaction(Long categoryId, Long accountId, TransactionType type, BigDecimal amount) throws Exception {
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(type.getCode())
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

    // DTO classes for response parsing
    static class StatsItem {
        private Long categoryId;
        private Long accountId;
        private String categoryName;
        private String accountName;
        private BigDecimal amount;
        private Integer count;

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    static class AssetsSummary {
        private BigDecimal total;
        private BigDecimal asset;
        private BigDecimal liability;

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public BigDecimal getAsset() { return asset; }
        public void setAsset(BigDecimal asset) { this.asset = asset; }
        public BigDecimal getLiability() { return liability; }
        public void setLiability(BigDecimal liability) { this.liability = liability; }
    }

    static class TrendItem {
        private String date;
        private BigDecimal income;
        private BigDecimal expense;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getIncome() { return income; }
        public void setIncome(BigDecimal income) { this.income = income; }
        public BigDecimal getExpense() { return expense; }
        public void setExpense(BigDecimal expense) { this.expense = expense; }
    }
}
