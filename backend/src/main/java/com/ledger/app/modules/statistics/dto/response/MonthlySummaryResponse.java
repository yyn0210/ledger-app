package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 月度收支概览响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "月度收支概览响应")
public class MonthlySummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "3")
    private Integer month;

    @Schema(description = "总收入", example = "10000.00")
    private BigDecimal totalIncome;

    @Schema(description = "总支出", example = "5000.00")
    private BigDecimal totalExpense;

    @Schema(description = "结余", example = "5000.00")
    private BigDecimal surplus;

    @Schema(description = "支出笔数", example = "50")
    private Long expenseCount;

    @Schema(description = "收入笔数", example = "5")
    private Long incomeCount;

    @Schema(description = "日均支出", example = "161.29")
    private BigDecimal dailyAverage;

    @Schema(description = "最高支出分类")
    private TopCategory topCategory;

    @Schema(description = "预算执行情况")
    private BudgetExecution budgetExecution;

    /**
     * 最高分类
     */
    @Data
    @Builder
    @Schema(description = "最高分类")
    public static class TopCategory implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "分类 ID", example = "1")
        private Long categoryId;

        @Schema(description = "分类名称", example = "餐饮")
        private String categoryName;

        @Schema(description = "金额", example = "2000.00")
        private BigDecimal amount;

        @Schema(description = "百分比", example = "40.00")
        private BigDecimal percentage;
    }

    /**
     * 预算执行
     */
    @Data
    @Builder
    @Schema(description = "预算执行")
    public static class BudgetExecution implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "总预算", example = "8000.00")
        private BigDecimal totalBudget;

        @Schema(description = "已支出", example = "5000.00")
        private BigDecimal spentAmount;

        @Schema(description = "执行进度", example = "62.50")
        private BigDecimal progress;

        @Schema(description = "剩余", example = "3000.00")
        private BigDecimal remaining;
    }
}
