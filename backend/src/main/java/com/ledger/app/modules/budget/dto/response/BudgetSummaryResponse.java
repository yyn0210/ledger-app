package com.ledger.app.modules.budget.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 预算汇总统计响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "预算汇总统计响应")
public class BudgetSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总预算金额", example = "10000.00")
    private BigDecimal totalBudget;

    @Schema(description = "总支出金额", example = "7500.00")
    private BigDecimal totalSpent;

    @Schema(description = "总体执行进度（百分比）", example = "75.00")
    private BigDecimal overallProgress;

    @Schema(description = "预算数量", example = "5")
    private Integer budgetCount;

    @Schema(description = "超支预算数量", example = "1")
    private Integer overBudgetCount;

    @Schema(description = "预警预算数量", example = "2")
    private Integer warningCount;

    @Schema(description = "按分类分组统计")
    private List<CategoryBudget> byCategory;

    /**
     * 分类预算统计
     */
    @Data
    @Builder
    @Schema(description = "分类预算统计")
    public static class CategoryBudget implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "分类 ID", example = "1")
        private Long categoryId;

        @Schema(description = "分类名称", example = "餐饮")
        private String categoryName;

        @Schema(description = "预算金额", example = "2000.00")
        private BigDecimal budgetAmount;

        @Schema(description = "实际支出", example = "1500.00")
        private BigDecimal spentAmount;

        @Schema(description = "执行进度（百分比）", example = "75.00")
        private BigDecimal progress;

        @Schema(description = "状态", example = "active")
        private String status;
    }
}
