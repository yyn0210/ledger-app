package com.ledger.app.modules.recurring.enums;

/**
 * 周期账单周期类型枚举
 */
public enum BillPeriod {
    DAILY("每日"),
    WEEKLY("每周"),
    MONTHLY("每月"),
    YEARLY("每年");

    private final String description;

    BillPeriod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
