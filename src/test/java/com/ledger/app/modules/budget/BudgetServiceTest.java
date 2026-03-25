package com.ledger.app.modules.budget;

import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.enums.BudgetPeriod;
import com.ledger.app.modules.budget.enums.BudgetStatus;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.budget.service.impl.BudgetServiceImpl;
import com.ledger.app.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 预算服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private CreateBudgetRequest createRequest;
    private Budget budget;

    @BeforeEach
    void setUp() {
        createRequest = CreateBudgetRequest.builder()
                .bookId(1L)
                .categoryId(1L)
                .amount(new BigDecimal("5000.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        budget = Budget.builder()
                .id(1L)
                .bookId(1L)
                .categoryId(1L)
                .amount(new BigDecimal("5000.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .spentAmount(new BigDecimal("1000.00"))
                .status(BudgetStatus.ACTIVE.getCode())
                .build();
    }

    @Test
    void testCreateBudget() {
        // 准备
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // 执行
        Long budgetId = budgetService.create(createRequest, 1L);

        // 验证
        assertNotNull(budgetId);
        assertEquals(1L, budgetId);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void testGetBudgetById() {
        // 准备
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // 执行
        Budget result = budgetService.getById(1L, 1L);

        // 验证
        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getAmount());
        assertEquals(new BigDecimal("1000.00"), result.getSpentAmount());
    }

    @Test
    void testGetBudgetById_NotFound() {
        // 准备
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> budgetService.getById(1L, 1L));
    }

    @Test
    void testGetBudgetsByBookId() {
        // 准备
        List<Budget> budgets = Arrays.asList(budget);
        when(budgetRepository.findByBookId(1L, null, null)).thenReturn(budgets);

        // 执行
        List<Budget> result = budgetService.getBudgetsByBookId(1L, null, null);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateBudget() {
        // 准备
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // 执行
        budget.setAmount(new BigDecimal("6000.00"));
        budgetService.update(1L, createRequest, 1L);

        // 验证
        verify(budgetRepository, times(1)).save(budget);
        assertEquals(new BigDecimal("6000.00"), budget.getAmount());
    }

    @Test
    void testDeleteBudget() {
        // 准备
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // 执行
        budgetService.delete(1L, 1L);

        // 验证
        verify(budgetRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateBudgetStatus() {
        // 准备
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        // 执行
        budgetService.updateBudgetStatus(1L);

        // 验证
        verify(budgetRepository, times(1)).save(budget);
    }
}
