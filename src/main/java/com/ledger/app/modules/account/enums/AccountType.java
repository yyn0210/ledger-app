package com.ledger.app.modules.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户类型枚举
 */
@Getter
@AllArgsConstructor
public enum AccountType {

    CASH(1, "现金", "💵"),
    BANK_CARD(2, "银行卡", "🏦"),
    CREDIT_CARD(3, "信用卡", "💳"),
    ALIPAY(4, "支付宝", "📱"),
    WECHAT(5, "微信", "💬"),
    OTHER(6, "其他", "📦");

    private final Integer code;
    private final String name;
    private final String icon;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 账户类型
     */
    public static AccountType fromCode(Integer code) {
        for (AccountType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + code);
    }
}
