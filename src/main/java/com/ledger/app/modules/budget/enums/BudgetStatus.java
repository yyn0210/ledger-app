package com.ledger.app.modules.budget.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预算状态枚举
 */
@Getter
@AllArgsConstructor
public enum BudgetStatus {

    ACTIVE("active", "进行中"),
    COMPLETED("completed", "已完成"),
    OVERDUE("overdue", "已超支");

    private final String code;
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 状态代码
     * @return 预算状态
     */
    public static BudgetStatus fromCode(String code) {
        for (BudgetStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid budget status: " + code);
    }
}
