package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算执行对比响应 DTO
 */
@Data
public class BudgetExecutionResponse {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer month;

    /**
     * 预算列表
     */
    private List<BudgetExecutionItem> budgets;

    /**
     * 总预算
     */
    private BigDecimal totalBudget;

    /**
     * 总支出
     */
    private BigDecimal totalSpent;

    /**
     * 总体进度
     */
    private BigDecimal overallProgress;

    /**
     * 预算执行项 DTO
     */
    @Data
    public static class BudgetExecutionItem {
        /**
         * 预算 ID
         */
        private Long budgetId;

        /**
         * 分类 ID
         */
        private Long categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 预算金额
         */
        private BigDecimal budgetAmount;

        /**
         * 已支出金额
         */
        private BigDecimal spentAmount;

        /**
         * 剩余金额
         */
        private BigDecimal remaining;

        /**
         * 进度百分比
         */
        private BigDecimal progress;

        /**
         * 状态
         */
        private String status;
    }
}
