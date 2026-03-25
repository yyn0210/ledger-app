package com.ledger.app.modules.savings.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建储蓄目标请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingsGoalRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 目标名称
     */
    @NotBlank(message = "目标名称不能为空")
    private String name;

    /**
     * 目标金额
     */
    @NotNull(message = "目标金额不能为空")
    @DecimalMin(value = "0.01", message = "目标金额必须大于 0")
    private BigDecimal targetAmount;

    /**
     * 目标日期
     */
    @NotNull(message = "目标日期不能为空")
    private LocalDate targetDate;

    /**
     * 每月存入金额（可选）
     */
    @DecimalMin(value = "0.01", message = "每月存入金额必须大于 0")
    private BigDecimal monthlyAmount;

    /**
     * 关联账户 ID（可选）
     */
    private Long accountId;

    /**
     * 备注
     */
    private String note;
}
