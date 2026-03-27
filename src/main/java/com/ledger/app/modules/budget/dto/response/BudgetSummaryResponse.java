package com.ledger.app.modules.budget.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算汇总统计响应 DTO
 */
@Data
public class BudgetSummaryResponse {

    /**
     * 总预算金额
     */
    private BigDecimal totalBudget;

    /**
     * 总支出金额
     */
    private BigDecimal totalSpent;

    /**
     * 总体进度百分比
     */
    private BigDecimal overallProgress;

    /**
     * 预算数量
     */
    private Integer budgetCount;

    /**
     * 超支预算数量
     */
    private Integer overBudgetCount;

    /**
     * 预警预算数量
     */
    private Integer warningCount;

    /**
     * 按分类分组的预算
     */
    private List<CategoryBudget> byCategory;

    /**
     * 分类预算 DTO
     */
    @Data
    public static class CategoryBudget {
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
         * 进度百分比
         */
        private BigDecimal progress;

        /**
         * 状态
         */
        private String status;
    }
}
