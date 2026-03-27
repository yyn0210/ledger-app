package com.ledger.app.modules.transaction.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交易类型枚举
 */
@Getter
@AllArgsConstructor
public enum TransactionType {

    INCOME(1, "收入"),
    EXPENSE(2, "支出"),
    TRANSFER(3, "转账");

    private final Integer code;
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 交易类型
     */
    public static TransactionType fromCode(Integer code) {
        for (TransactionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type: " + code);
    }
}
