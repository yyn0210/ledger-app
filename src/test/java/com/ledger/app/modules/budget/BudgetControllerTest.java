package com.ledger.app.modules.budget;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.budget.controller.BudgetController;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetAlertResponse;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc mockMvc;
    private BudgetResponse testBudget;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();

        testBudget = new BudgetResponse();
        testBudget.setId(1L);
        testBudget.setBookId(100L);
        testBudget.setName("测试预算");
        testBudget.setAmount(new BigDecimal("2000.00"));
        testBudget.setPeriod("monthly");
        testBudget.setStatus("active");
    }

    @Test
    void testGetBudgets() throws Exception {
        // Arrange
        List<BudgetResponse> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetService.getBudgets(100L, 100L, null, null)).thenReturn(budgets);

        // Act & Assert
        mockMvc.perform(get("/api/budgets")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(budgetService).getBudgets(100L, 100L, null, null);
    }

    @Test
    void testGetBudgets_ByPeriod() throws Exception {
        // Arrange
        List<BudgetResponse> budgets = new ArrayList<>();
        budgets.add(testBudget);
        when(budgetService.getBudgets(100L, 100L, "monthly", null)).thenReturn(budgets);

        // Act & Assert
        mockMvc.perform(get("/api/budgets")
                .param("bookId", "100")
                .param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(budgetService).getBudgets(100L, 100L, "monthly", null);
    }

    @Test
    void testGetBudgetDetail() throws Exception {
        // Arrange
        BudgetDetailResponse detail = new BudgetDetailResponse();
        detail.setId(1L);
        detail.setName("测试预算");
        detail.setAmount(new BigDecimal("2000.00"));
        when(budgetService.getBudgetDetail(1L, 100L, 100L)).thenReturn(detail);

        // Act & Assert
        mockMvc.perform(get("/api/budgets/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试预算"));

        verify(budgetService).getBudgetDetail(1L, 100L, 100L);
    }

    @Test
    void testCreateBudget() throws Exception {
        // Arrange
        when(budgetService.createBudget(any(CreateBudgetRequest.class))).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":100,\"name\":\"新预算\",\"amount\":2000.00,\"period\":\"monthly\",\"startDate\":\"2026-04-01\",\"endDate\":\"2026-04-30\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(budgetService).createBudget(any(CreateBudgetRequest.class));
    }

    @Test
    void testUpdateBudget() throws Exception {
        // Arrange
        doNothing().when(budgetService).updateBudget(anyLong(), any(UpdateBudgetRequest.class), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/budgets/1")
                .param("bookId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新预算\",\"amount\":2500.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(budgetService).updateBudget(anyLong(), any(UpdateBudgetRequest.class), anyLong(), anyLong());
    }

    @Test
    void testDeleteBudget() throws Exception {
        // Arrange
        doNothing().when(budgetService).deleteBudget(anyLong(), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/budgets/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(budgetService).deleteBudget(1L, 100L, 100L);
    }

    @Test
    void testCheckBudgetAlerts() throws Exception {
        // Arrange
        List<BudgetAlertResponse> alerts = new ArrayList<>();
        BudgetAlertResponse alert = new BudgetAlertResponse();
        alert.setBudgetId(1L);
        alert.setAlertType("warning");
        alert.setMessage("预算使用率已达 85.00%");
        alerts.add(alert);

        when(budgetService.checkBudgetAlerts(100L, 100L)).thenReturn(alerts);

        // Act & Assert
        mockMvc.perform(get("/api/budgets/alerts")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].alertType").value("warning"));

        verify(budgetService).checkBudgetAlerts(100L, 100L);
    }
}
