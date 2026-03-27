package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 月度概览响应 DTO
 */
@Data
public class MonthlySummaryResponse {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 总收入
     */
    private BigDecimal totalIncome;

    /**
     * 总支出
     */
    private BigDecimal totalExpense;

    /**
     * 结余
     */
    private BigDecimal surplus;

    /**
     * 支出笔数
     */
    private Integer expenseCount;

    /**
     * 收入笔数
     */
    private Integer incomeCount;

    /**
     * 日均支出
     */
    private BigDecimal dailyAverage;

    /**
     * 最高支出分类
     */
    private TopCategory topCategory;

    /**
     * 预算执行情况
     */
    private BudgetExecution budgetExecution;

    /**
     * 最高分类 DTO
     */
    @Data
    public static class TopCategory {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
        private BigDecimal percentage;
    }

    /**
     * 预算执行 DTO
     */
    @Data
    public static class BudgetExecution {
        private BigDecimal totalBudget;
        private BigDecimal spentAmount;
        private BigDecimal progress;
        private BigDecimal remaining;
    }
}
