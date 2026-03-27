package com.ledger.app.modules.statistics;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.statistics.controller.StatisticsController;
import com.ledger.app.modules.statistics.dto.response.*;
import com.ledger.app.modules.statistics.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 统计控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();
    }

    @Test
    void testGetExpenseByCategory() throws Exception {
        // Arrange
        CategoryStatisticsSummaryResponse response = new CategoryStatisticsSummaryResponse();
        response.setTotalAmount(new BigDecimal("500.00"));
        when(statisticsService.getExpenseByCategory(anyLong(), anyLong(), any(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/expense-by-category")
                .param("bookId", "100")
                .param("startDate", "2026-03-01")
                .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getExpenseByCategory(anyLong(), anyLong(), any(), any());
    }

    @Test
    void testGetIncomeByCategory() throws Exception {
        // Arrange
        CategoryStatisticsSummaryResponse response = new CategoryStatisticsSummaryResponse();
        response.setTotalAmount(new BigDecimal("1000.00"));
        when(statisticsService.getIncomeByCategory(anyLong(), anyLong(), any(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/income-by-category")
                .param("bookId", "100")
                .param("startDate", "2026-03-01")
                .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getIncomeByCategory(anyLong(), anyLong(), any(), any());
    }

    @Test
    void testGetTrend() throws Exception {
        // Arrange
        TrendSummaryResponse response = new TrendSummaryResponse();
        response.setTrend(new ArrayList<>());
        when(statisticsService.getTrend(anyLong(), anyLong(), anyString(), any(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/trend")
                .param("bookId", "100")
                .param("type", "monthly")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getTrend(anyLong(), anyLong(), anyString(), any(), any());
    }

    @Test
    void testGetRanking() throws Exception {
        // Arrange
        RankingSummaryResponse response = new RankingSummaryResponse();
        response.setType("expense");
        response.setRanking(new ArrayList<>());
        when(statisticsService.getRanking(anyLong(), anyLong(), anyString(), anyInt(), any(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/ranking")
                .param("bookId", "100")
                .param("type", "expense")
                .param("limit", "10")
                .param("startDate", "2026-03-01")
                .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getRanking(anyLong(), anyLong(), anyString(), anyInt(), any(), any());
    }

    @Test
    void testGetAssetsSummary() throws Exception {
        // Arrange
        AssetsSummaryResponse response = new AssetsSummaryResponse();
        response.setTotalAssets(new BigDecimal("50000.00"));
        when(statisticsService.getAssetsSummary(anyLong(), anyLong())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/assets")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getAssetsSummary(anyLong(), anyLong());
    }

    @Test
    void testGetMonthlySummary() throws Exception {
        // Arrange
        MonthlySummaryResponse response = new MonthlySummaryResponse();
        response.setYear(2026);
        response.setMonth(3);
        when(statisticsService.getMonthlySummary(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/monthly-summary")
                .param("bookId", "100")
                .param("year", "2026")
                .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getMonthlySummary(anyLong(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void testGetYearlySummary() throws Exception {
        // Arrange
        YearlySummaryResponse response = new YearlySummaryResponse();
        response.setYear(2026);
        when(statisticsService.getYearlySummary(anyLong(), anyLong(), anyInt())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/yearly-summary")
                .param("bookId", "100")
                .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getYearlySummary(anyLong(), anyLong(), anyInt());
    }

    @Test
    void testGetBudgetExecution() throws Exception {
        // Arrange
        BudgetExecutionResponse response = new BudgetExecutionResponse();
        response.setYear(2026);
        response.setMonth(3);
        when(statisticsService.getBudgetExecution(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/statistics/budget-execution")
                .param("bookId", "100")
                .param("year", "2026")
                .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(statisticsService).getBudgetExecution(anyLong(), anyLong(), anyInt(), anyInt());
    }
}
