package com.ledger.app.modules.budget.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算预警响应 DTO
 */
@Data
public class BudgetAlertResponse {

    /**
     * 预算 ID
     */
    private Long budgetId;

    /**
     * 预算名称
     */
    private String budgetName;

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
    private BigDecimal amount;

    /**
     * 已支出金额
     */
    private BigDecimal spentAmount;

    /**
     * 进度百分比
     */
    private BigDecimal progress;

    /**
     * 预警阈值（百分比）
     */
    private BigDecimal alertThreshold;

    /**
     * 预警类型：warning/overdue
     */
    private String alertType;

    /**
     * 预警消息
     */
    private String message;
}
