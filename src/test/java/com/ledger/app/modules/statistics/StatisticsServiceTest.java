package com.ledger.app.modules.statistics;

import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.statistics.dto.response.*;
import com.ledger.app.modules.statistics.service.impl.StatisticsServiceImpl;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 统计服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private Transaction expenseTx;
    private Transaction incomeTx;
    private Account testAccount;
    private Category testCategory;
    private Budget testBudget;

    @BeforeEach
    void setUp() {
        expenseTx = new Transaction();
        expenseTx.setId(1L);
        expenseTx.setBookId(100L);
        expenseTx.setType(2); // 支出
        expenseTx.setAmount(new BigDecimal("50.00"));
        expenseTx.setCategoryId(1L);
        expenseTx.setTitle("测试支出");
        expenseTx.setTransactionDate(LocalDate.now());

        incomeTx = new Transaction();
        incomeTx.setId(2L);
        incomeTx.setBookId(100L);
        incomeTx.setType(1); // 收入
        incomeTx.setAmount(new BigDecimal("1000.00"));
        incomeTx.setCategoryId(10L);
        incomeTx.setTitle("测试收入");
        incomeTx.setTransactionDate(LocalDate.now());

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setBookId(100L);
        testAccount.setName("测试账户");
        testAccount.setType(2); // 银行卡
        testAccount.setBalance(new BigDecimal("5000.00"));

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("餐饮");
        testCategory.setIcon("🍜");

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setBookId(100L);
        testBudget.setCategoryId(1L);
        testBudget.setAmount(new BigDecimal("2000.00"));
    }

    @Test
    void testGetExpenseByCategory_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(expenseTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);
        when(categoryRepository.selectById(1L)).thenReturn(testCategory);

        // Act
        CategoryStatisticsSummaryResponse result = statisticsService.getExpenseByCategory(
                100L, 100L, LocalDate.now().minusMonths(1), LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCategories());
        assertEquals(1, result.getCategories().size());
        assertEquals(new BigDecimal("50.00"), result.getCategories().get(0).getAmount());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetIncomeByCategory_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(incomeTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);
        when(categoryRepository.selectById(10L)).thenReturn(testCategory);

        // Act
        CategoryStatisticsSummaryResponse result = statisticsService.getIncomeByCategory(
                100L, 100L, LocalDate.now().minusMonths(1), LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCategories());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetTrend_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(expenseTx);
        transactions.add(incomeTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);

        // Act
        TrendSummaryResponse result = statisticsService.getTrend(
                100L, 100L, "monthly", LocalDate.now().minusMonths(3), LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTrend());
        assertFalse(result.getTrend().isEmpty());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetRanking_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(expenseTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);
        when(categoryRepository.selectById(1L)).thenReturn(testCategory);

        // Act
        RankingSummaryResponse result = statisticsService.getRanking(
                100L, 100L, "expense", 10, LocalDate.now().minusMonths(1), LocalDate.now()
        );

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRanking());
        assertEquals("expense", result.getType());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetAssetsSummary_Success() {
        // Arrange
        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);
        when(accountRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(accounts);

        // Act
        AssetsSummaryResponse result = statisticsService.getAssetsSummary(100L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getTotalAssets());
        assertEquals(1, result.getAccountCount());
        verify(accountRepository).selectByBookIdAndUserId(100L, 100L);
    }

    @Test
    void testGetMonthlySummary_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(expenseTx);
        transactions.add(incomeTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);
        when(categoryRepository.selectById(anyLong())).thenReturn(testCategory);
        when(budgetRepository.selectByBookIdAndUserId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        // Act
        MonthlySummaryResponse result = statisticsService.getMonthlySummary(100L, 100L, 2026, 3);

        // Assert
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(3, result.getMonth());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetYearlySummary_Success() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(expenseTx);
        transactions.add(incomeTx);
        when(transactionRepository.selectList(any())).thenReturn(transactions);
        when(categoryRepository.selectById(anyLong())).thenReturn(testCategory);

        // Act
        YearlySummaryResponse result = statisticsService.getYearlySummary(100L, 100L, 2026);

        // Assert
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertNotNull(result.getTotalIncome());
        assertNotNull(result.getTotalExpense());
        verify(transactionRepository).selectList(any());
    }

    @Test
    void testGetBudgetExecution_Success() {
        // Arrange
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(budgets);
        when(transactionRepository.selectList(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.selectById(1L)).thenReturn(testCategory);

        // Act
        BudgetExecutionResponse result = statisticsService.getBudgetExecution(100L, 100L, 2026, 3);

        // Assert
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(3, result.getMonth());
        assertNotNull(result.getBudgets());
        verify(budgetRepository).selectByBookIdAndUserId(100L, 100L);
    }
}
