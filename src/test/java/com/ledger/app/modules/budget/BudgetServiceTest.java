package com.ledger.app.modules.budget;

import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetAlertResponse;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.budget.service.impl.BudgetServiceImpl;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预算服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private Budget testBudget;
    private CreateBudgetRequest createRequest;

    @BeforeEach
    void setUp() {
        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setBookId(100L);
        testBudget.setUserId(100L);
        testBudget.setName("测试预算");
        testBudget.setCategoryId(1L);
        testBudget.setAmount(new BigDecimal("2000.00"));
        testBudget.setPeriod("monthly");
        testBudget.setStartDate(LocalDate.of(2026, 3, 1));
        testBudget.setEndDate(LocalDate.of(2026, 3, 31));
        testBudget.setAlertThreshold(new BigDecimal("80.00"));
        testBudget.setStatus("active");

        createRequest = new CreateBudgetRequest();
        createRequest.setBookId(100L);
        createRequest.setUserId(100L);
        createRequest.setName("新预算");
        createRequest.setCategoryId(1L);
        createRequest.setAmount(new BigDecimal("3000.00"));
        createRequest.setPeriod("monthly");
        createRequest.setStartDate(LocalDate.of(2026, 4, 1));
        createRequest.setEndDate(LocalDate.of(2026, 4, 30));
        createRequest.setAlertThreshold(new BigDecimal("80.00"));
    }

    @Test
    void testGetBudgets_Success() {
        // Arrange
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(budgets);
        when(budgetRepository.sumByBookIdAndCategoryIdAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("1500.00"));

        // Act
        List<BudgetResponse> result = budgetService.getBudgets(100L, 100L, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试预算", result.get(0).getName());
        verify(budgetRepository).selectByBookIdAndUserId(100L, 100L);
    }

    @Test
    void testGetBudgets_ByPeriod() {
        // Arrange
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetRepository.selectByBookIdAndUserIdAndPeriod(100L, 100L, "monthly")).thenReturn(budgets);
        when(budgetRepository.sumByBookIdAndCategoryIdAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("1500.00"));

        // Act
        List<BudgetResponse> result = budgetService.getBudgets(100L, 100L, "monthly", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(budgetRepository).selectByBookIdAndUserIdAndPeriod(100L, 100L, "monthly");
    }

    @Test
    void testGetBudgetDetail_Success() {
        // Arrange
        when(budgetRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testBudget);
        when(budgetRepository.sumByBookIdAndCategoryIdAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("1500.00"));

        // Act
        BudgetDetailResponse result = budgetService.getBudgetDetail(1L, 100L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals("测试预算", result.getName());
        verify(budgetRepository).selectByIdAndBookId(1L, 100L);
    }

    @Test
    void testGetBudgetDetail_NotFound() {
        // Arrange
        when(budgetRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.getBudgetDetail(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("预算不存在"));
    }

    @Test
    void testCreateBudget_Success() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("测试分类");
        when(categoryRepository.selectByIdAndBookId(1L, 100L)).thenReturn(category);
        when(budgetRepository.insert(any(Budget.class))).thenReturn(1);

        // Act
        Long result = budgetService.createBudget(createRequest);

        // Assert
        assertNotNull(result);
        verify(budgetRepository).insert(any(Budget.class));
    }

    @Test
    void testCreateBudget_CategoryNotFound() {
        // Arrange
        when(categoryRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.createBudget(createRequest);
        });
        assertTrue(exception.getMessage().contains("分类不存在"));
    }

    @Test
    void testCreateBudget_InvalidDateRange() {
        // Arrange
        createRequest.setStartDate(LocalDate.of(2026, 4, 30));
        createRequest.setEndDate(LocalDate.of(2026, 4, 1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.createBudget(createRequest);
        });
        assertTrue(exception.getMessage().contains("开始日期不能晚于结束日期"));
    }

    @Test
    void testUpdateBudget_Success() {
        // Arrange
        when(budgetRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testBudget);
        when(budgetRepository.updateById(any(Budget.class))).thenReturn(1);

        UpdateBudgetRequest updateRequest = new UpdateBudgetRequest();
        updateRequest.setName("更新后的预算");
        updateRequest.setAmount(new BigDecimal("2500.00"));

        // Act
        budgetService.updateBudget(1L, updateRequest, 100L, 100L);

        // Assert
        verify(budgetRepository).selectByIdAndBookId(1L, 100L);
        verify(budgetRepository).updateById(any(Budget.class));
    }

    @Test
    void testUpdateBudget_NotFound() {
        // Arrange
        when(budgetRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        UpdateBudgetRequest updateRequest = new UpdateBudgetRequest();
        updateRequest.setName("更新后的预算");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            budgetService.updateBudget(1L, updateRequest, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("预算不存在"));
    }

    @Test
    void testDeleteBudget_Success() {
        // Arrange
        when(budgetRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testBudget);
        when(budgetRepository.deleteById(1L)).thenReturn(1);

        // Act
        budgetService.deleteBudget(1L, 100L, 100L);

        // Assert
        verify(budgetRepository).deleteById(1L);
    }

    @Test
    void testCheckBudgetAlerts_OverBudget() {
        // Arrange
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(budgets);
        when(budgetRepository.sumByBookIdAndCategoryIdAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("2500.00")); // 超支 500

        // Act
        List<BudgetAlertResponse> alerts = budgetService.checkBudgetAlerts(100L, 100L);

        // Assert
        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertEquals("overdue", alerts.get(0).getAlertType());
    }

    @Test
    void testCheckBudgetAlerts_Warning() {
        // Arrange
        List<Budget> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(budgets);
        when(budgetRepository.sumByBookIdAndCategoryIdAndDateRange(anyLong(), anyLong(), any(), any()))
                .thenReturn(new BigDecimal("1700.00")); // 85%，超过 80% 预警线

        // Act
        List<BudgetAlertResponse> alerts = budgetService.checkBudgetAlerts(100L, 100L);

        // Assert
        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertEquals("warning", alerts.get(0).getAlertType());
    }
}
