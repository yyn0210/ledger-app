package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 年度概览响应 DTO
 */
@Data
public class YearlySummaryResponse {

    /**
     * 年份
     */
    private Integer year;

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
     * 月均统计
     */
    private MonthlyAverage monthlyAverage;

    /**
     * 最高支出分类
     */
    private TopCategory topExpenseCategory;

    /**
     * 最高收入分类
     */
    private TopCategory topIncomeCategory;

    /**
     * 月均统计 DTO
     */
    @Data
    public static class MonthlyAverage {
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal surplus;
    }

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
}
