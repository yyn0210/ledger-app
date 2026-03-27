package com.ledger.app.modules.recurring.enums;

/**
 * 周期账单状态枚举
 */
public enum BillStatus {
    ACTIVE("进行中"),
    PAUSED("已暂停"),
    COMPLETED("已完成");

    private final String description;

    BillStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
