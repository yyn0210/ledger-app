package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 预算执行对比响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "预算执行对比响应")
public class BudgetExecutionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "3")
    private Integer month;

    @Schema(description = "预算列表")
    private List<BudgetItem> budgets;

    @Schema(description = "总预算", example = "8000.00")
    private BigDecimal totalBudget;

    @Schema(description = "总支出", example = "5000.00")
    private BigDecimal totalSpent;

    @Schema(description = "总体执行进度", example = "62.50")
    private BigDecimal overallProgress;

    /**
     * 预算项
     */
    @Data
    @Builder
    @Schema(description = "预算项")
    public static class BudgetItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "预算 ID", example = "1")
        private Long budgetId;

        @Schema(description = "分类 ID", example = "1")
        private Long categoryId;

        @Schema(description = "分类名称", example = "餐饮")
        private String categoryName;

        @Schema(description = "预算金额", example = "2000.00")
        private BigDecimal budgetAmount;

        @Schema(description = "已支出", example = "1500.00")
        private BigDecimal spentAmount;

        @Schema(description = "剩余", example = "500.00")
        private BigDecimal remaining;

        @Schema(description = "执行进度", example = "75.00")
        private BigDecimal progress;

        @Schema(description = "状态", example = "active")
        private String status;
    }
}
