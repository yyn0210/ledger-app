package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 年度统计概览响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "年度统计概览响应")
public class YearlySummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "总收入", example = "120000.00")
    private BigDecimal totalIncome;

    @Schema(description = "总支出", example = "80000.00")
    private BigDecimal totalExpense;

    @Schema(description = "结余", example = "40000.00")
    private BigDecimal surplus;

    @Schema(description = "支出笔数", example = "500")
    private Long expenseCount;

    @Schema(description = "收入笔数", example = "50")
    private Long incomeCount;

    @Schema(description = "月均收入", example = "10000.00")
    private BigDecimal monthlyAverageIncome;

    @Schema(description = "月均支出", example = "6666.67")
    private BigDecimal monthlyAverageExpense;

    @Schema(description = "月均结余", example = "3333.33")
    private BigDecimal monthlyAverageSurplus;

    @Schema(description = "最高支出分类")
    private TopCategory topExpenseCategory;

    @Schema(description = "最高收入分类")
    private TopCategory topIncomeCategory;

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

        @Schema(description = "金额", example = "24000.00")
        private BigDecimal amount;

        @Schema(description = "百分比", example = "30.00")
        private BigDecimal percentage;
    }
}
