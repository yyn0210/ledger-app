package com.ledger.app.modules.budget.enums;

import lombok.Getter;

/**
 * 预算周期枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum BudgetPeriod {

    /**
     * 月度预算
     */
    MONTHLY("monthly", "月度预算"),

    /**
     * 年度预算
     */
    YEARLY("yearly", "年度预算"),

    /**
     * 自定义周期
     */
    CUSTOM("custom", "自定义周期");

    private final String code;
    private final String description;

    BudgetPeriod(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static BudgetPeriod fromCode(String code) {
        for (BudgetPeriod period : values()) {
            if (period.code.equals(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid budget period: " + code);
    }
}
