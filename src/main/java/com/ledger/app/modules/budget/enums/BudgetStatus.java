package com.ledger.app.modules.budget.enums;

import lombok.Getter;

/**
 * 预算状态枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum BudgetStatus {

    /**
     * 进行中
     */
    ACTIVE("active", "进行中"),

    /**
     * 已完成
     */
    COMPLETED("completed", "已完成"),

    /**
     * 已超支
     */
    OVERDUE("overdue", "已超支");

    private final String code;
    private final String description;

    BudgetStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static BudgetStatus fromCode(String code) {
        for (BudgetStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid budget status: " + code);
    }
}
