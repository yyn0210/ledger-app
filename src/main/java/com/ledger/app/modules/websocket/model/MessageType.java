package com.ledger.app.modules.websocket.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * WebSocket 消息类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum MessageType {

    // 交易相关
    TRANSACTION_CREATED("transaction.created", "交易创建"),
    TRANSACTION_UPDATED("transaction.updated", "交易更新"),
    TRANSACTION_DELETED("transaction.deleted", "交易删除"),

    // 账户相关
    ACCOUNT_UPDATED("account.updated", "账户更新"),
    BALANCE_CHANGED("account.balance_changed", "余额变更"),

    // 预算相关
    BUDGET_ALERT("budget.alert", "预算预警"),
    BUDGET_OVERDUE("budget.overdue", "预算超支"),

    // 周期账单相关
    RECURRING_BILL_EXECUTED("recurring.executed", "周期账单执行"),

    // 储蓄目标相关
    SAVINGS_PROGRESS("savings.progress", "储蓄进度更新"),
    SAVINGS_COMPLETED("savings.completed", "储蓄目标完成"),

    // 系统相关
    SYSTEM_NOTIFICATION("system.notification", "系统通知"),
    SYNC_REQUEST("sync.request", "同步请求"),
    SYNC_RESPONSE("sync.response", "同步响应");

    @JsonValue
    private final String code;

    private final String name;

    MessageType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MessageType fromCode(String code) {
        for (MessageType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SYSTEM_NOTIFICATION;
    }
}
