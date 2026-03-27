package com.ledger.app.modules.budget.dto.request;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建预算请求 DTO
 */
@Data
public class CreateBudgetRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 用户 ID（从 JWT 解析）
     */
    private Long userId;

    /**
     * 预算名称
     */
    @NotBlank(message = "预算名称不能为空")
    private String name;

    /**
     * 分类 ID（NULL 表示总预算）
     */
    private Long categoryId;

    /**
     * 预算金额
     */
    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于 0")
    private BigDecimal amount;

    /**
     * 周期：monthly/yearly/custom
     */
    @NotBlank(message = "预算周期不能为空")
    private String period;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    /**
     * 预警阈值（百分比）
     */
    private BigDecimal alertThreshold;
}
