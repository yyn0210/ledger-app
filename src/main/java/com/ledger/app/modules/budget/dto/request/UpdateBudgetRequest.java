package com.ledger.app.modules.budget.dto.request;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 更新预算请求 DTO
 */
@Data
public class UpdateBudgetRequest {

    /**
     * 预算名称
     */
    private String name;

    /**
     * 预算金额
     */
    @DecimalMin(value = "0.00", message = "预算金额不能为负数")
    private BigDecimal amount;

    /**
     * 预警阈值（百分比）
     */
    private BigDecimal alertThreshold;
}
