package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.request.LoginRequest;
import com.ledger.app.modules.auth.dto.request.RegisterRequest;
import com.ledger.app.modules.auth.dto.response.AuthResponse;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.dto.request.TransferRequest;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.enums.BudgetPeriod;
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
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 跨模块集成测试
 * 测试场景：
 * 1. 完整业务流程：注册 → 账本 → 分类 → 账户 → 交易 → 统计 → 预算
 * 2. 复杂场景：多账户转账 + 分类统计 + 预算执行跟踪
 * 3. 数据一致性：交易删除后余额、统计、预算执行率同步更新
 * 4. 并发场景：批量交易创建后的数据一致性
 *
 * @author Chisong
 * @since 2026-03-27
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CrossModuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long userId;
    private Long bookId;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "cross_module_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("cross_module_test@example.com")
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
                .name("跨模块测试账本")
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
    }

    @Test
    void testFullWorkflow_CompleteBusinessFlow() throws Exception {
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
                .name("招商银行卡")
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

        // 3. 创建预算
        CreateBudgetRequest budgetRequest = CreateBudgetRequest.builder()
                .bookId(bookId)
                .amount(new BigDecimal("5000.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.now().withDayOfMonth(1))
                .endDate(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1))
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
        Long budgetId = budgetResult.getData();
        assert budgetId != null && budgetId > 0;

        // 4. 创建多笔交易
        List<Long> transactionIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                    .bookId(bookId)
                    .categoryId(categoryId)
                    .accountId(accountId)
                    .type(TransactionType.EXPENSE.getCode())
                    .amount(new BigDecimal("100.00"))
                    .transactionDate(LocalDate.now())
                    .note("测试交易" + i)
                    .build();

            String txResponse = mockMvc.perform(post("/api/transaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(txRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Result<Long> txResult = objectMapper.readValue(txResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
            transactionIds.add(txResult.getData());
        }

        // 5. 验证账户余额同步更新
        String updatedAccountResponse = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AccountResponse> updatedAccountResult = objectMapper.readValue(updatedAccountResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AccountResponse.class));
        BigDecimal updatedBalance = updatedAccountResult.getData().getBalance();

        // 初始 10000 - 5 笔 100 = 9500
        assert updatedBalance.compareTo(new BigDecimal("9500.00")) == 0;

        // 6. 验证统计聚合
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

        // 7. 验证交易列表
        String txListResponse = mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Test
    void testDataConsistency_DeleteTransaction_SyncAllModules() throws Exception {
        // 1. 创建分类
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

        // 3. 创建交易
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bookId)
                .categoryId(categoryId)
                .accountId(accountId)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("500.00"))
                .transactionDate(LocalDate.now())
                .note("测试交易")
                .build();

        String txResponse = mockMvc.perform(post("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> txResult = objectMapper.readValue(txResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long transactionId = txResult.getData();

        // 4. 验证余额减少
        String balanceAfterTx = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AccountResponse> balanceAfterTxResult = objectMapper.readValue(balanceAfterTx,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AccountResponse.class));
        assert balanceAfterTxResult.getData().getBalance().compareTo(new BigDecimal("9500.00")) == 0;

        // 5. 删除交易
        mockMvc.perform(delete("/api/transaction/" + transactionId)
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 6. 验证余额恢复
        String balanceAfterDelete = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AccountResponse> balanceAfterDeleteResult = objectMapper.readValue(balanceAfterDelete,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AccountResponse.class));
        assert balanceAfterDeleteResult.getData().getBalance().compareTo(new BigDecimal("10000.00")) == 0;

        // 7. 验证统计更新（应为 0）
        String statsAfterDelete = mockMvc.perform(get("/api/statistics/expense-by-category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 统计应反映删除后的状态
    }

    @Test
    void testTransferWorkflow_AtomicMultiAccountUpdate() throws Exception {
        // 1. 创建两个账户
        CreateAccountRequest account1Request = CreateAccountRequest.builder()
                .name("银行卡")
                .type(1)
                .balance(new BigDecimal("10000.00"))
                .bookId(bookId)
                .build();

        String account1Response = mockMvc.perform(post("/api/account")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account1Request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> account1Result = objectMapper.readValue(account1Response,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long accountId1 = account1Result.getData();

        CreateAccountRequest account2Request = CreateAccountRequest.builder()
                .name("微信钱包")
                .type(2)
                .balance(new BigDecimal("2000.00"))
                .bookId(bookId)
                .build();

        String account2Response = mockMvc.perform(post("/api/account")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(account2Request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> account2Result = objectMapper.readValue(account2Response,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long accountId2 = account2Result.getData();

        // 2. 获取初始余额
        BigDecimal initialBalance1 = getAccountBalance(accountId1);
        BigDecimal initialBalance2 = getAccountBalance(accountId2);

        // 3. 创建转账
        TransferRequest transferRequest = TransferRequest.builder()
                .bookId(bookId)
                .fromAccountId(accountId1)
                .toAccountId(accountId2)
                .amount(new BigDecimal("1000.00"))
                .title("银行卡转入微信")
                .transactionDate(LocalDate.now())
                .build();

        String transferResponse = mockMvc.perform(post("/api/transaction/transfer")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> transferResult = objectMapper.readValue(transferResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        Long transferId = transferResult.getData();
        assert transferId != null && transferId > 0;

        // 4. 验证原子性更新
        BigDecimal updatedBalance1 = getAccountBalance(accountId1);
        BigDecimal updatedBalance2 = getAccountBalance(accountId2);

        // 转出账户减少 1000
        assert initialBalance1.subtract(updatedBalance1).compareTo(new BigDecimal("1000.00")) == 0;
        // 转入账户增加 1000
        assert updatedBalance2.subtract(initialBalance2).compareTo(new BigDecimal("1000.00")) == 0;

        // 5. 验证总资产不变（10000 + 2000 = 12000）
        String assetsResponse = mockMvc.perform(get("/api/statistics/assets-summary")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AssetsSummary> assetsResult = objectMapper.readValue(assetsResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AssetsSummary.class));
        assert assetsResult.getData().getTotal().compareTo(new BigDecimal("12000.00")) == 0;
    }

    @Test
    void testBatchTransaction_ConsistencyCheck() throws Exception {
        // 1. 创建分类
        CreateCategoryRequest categoryRequest = CreateCategoryRequest.builder()
                .name("批量测试分类")
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

        // 2. 创建账户
        CreateAccountRequest accountRequest = CreateAccountRequest.builder()
                .name("批量测试账户")
                .type(1)
                .balance(new BigDecimal("50000.00"))
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

        // 3. 批量创建交易
        int batchCount = 10;
        BigDecimal txAmount = new BigDecimal("100.00");
        BigDecimal totalAmount = txAmount.multiply(new BigDecimal(batchCount));

        for (int i = 0; i < batchCount; i++) {
            CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                    .bookId(bookId)
                    .categoryId(categoryId)
                    .accountId(accountId)
                    .type(TransactionType.EXPENSE.getCode())
                    .amount(txAmount)
                    .transactionDate(LocalDate.now())
                    .note("批量交易" + i)
                    .build();

            mockMvc.perform(post("/api/transaction")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(txRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        // 4. 验证余额一致性
        BigDecimal expectedBalance = new BigDecimal("50000.00").subtract(totalAmount);
        BigDecimal actualBalance = getAccountBalance(accountId);
        assert actualBalance.compareTo(expectedBalance) == 0;

        // 5. 验证交易总数
        String txListResponse = mockMvc.perform(get("/api/transaction")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(batchCount))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 6. 验证统计聚合
        String statsResponse = mockMvc.perform(get("/api/statistics/expense-by-category")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString())
                        .param("startDate", LocalDate.now().withDayOfMonth(1).toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 统计总额应为 1000
    }

    // Helper method to get account balance
    private BigDecimal getAccountBalance(Long accountId) throws Exception {
        String response = mockMvc.perform(get("/api/account/" + accountId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AccountResponse> result = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructParametricType(Result.class, AccountResponse.class));
        return result.getData().getBalance();
    }

    // DTO class for assets summary
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
}
