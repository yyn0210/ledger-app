package com.ledger.app.modules.budget.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预算周期枚举
 */
@Getter
@AllArgsConstructor
public enum BudgetPeriod {

    MONTHLY("monthly", "月度预算"),
    YEARLY("yearly", "年度预算"),
    CUSTOM("custom", "自定义周期");

    private final String code;
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 周期代码
     * @return 预算周期
     */
    public static BudgetPeriod fromCode(String code) {
        for (BudgetPeriod period : values()) {
            if (period.getCode().equals(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Invalid budget period: " + code);
    }
}
