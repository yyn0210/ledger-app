package com.ledger.app.modules.savings.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 更新储蓄目标请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSavingsGoalRequest {

    /**
     * 目标名称
     */
    private String name;

    /**
     * 目标金额
     */
    @DecimalMin(value = "0.01", message = "目标金额必须大于 0")
    private BigDecimal targetAmount;

    /**
     * 已存金额
     */
    @DecimalMin(value = "0", message = "已存金额不能为负数")
    private BigDecimal savedAmount;

    /**
     * 每月存入金额
     */
    @DecimalMin(value = "0.01", message = "每月存入金额必须大于 0")
    private BigDecimal monthlyAmount;

    /**
     * 备注
     */
    private String note;

    /**
     * 状态：1-进行中 2-已完成 3-已暂停 4-已取消
     */
    private Integer status;
}
