package com.ledger.app.modules.recurring.enums;

/**
 * 周期账单执行状态枚举
 */
public enum ExecutionStatus {
    PENDING("待执行"),
    SUCCESS("执行成功"),
    FAILED("执行失败"),
    SKIPPED("已跳过");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
