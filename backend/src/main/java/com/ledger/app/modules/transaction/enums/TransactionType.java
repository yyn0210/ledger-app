package com.ledger.app.modules.transaction.enums;

import lombok.Getter;

/**
 * 交易类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum TransactionType {

    /**
     * 收入
     */
    INCOME(1, "收入"),

    /**
     * 支出
     */
    EXPENSE(2, "支出"),

    /**
     * 转账
     */
    TRANSFER(3, "转账");

    private final Integer code;
    private final String name;

    TransactionType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TransactionType fromCode(Integer code) {
        for (TransactionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: " + code);
    }
}
