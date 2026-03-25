package com.ledger.app.modules.account.enums;

import lombok.Getter;

/**
 * 账户类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum AccountType {

    /**
     * 现金
     */
    CASH(1, "现金", "💵"),

    /**
     * 银行卡
     */
    BANK_CARD(2, "银行卡", "🏦"),

    /**
     * 信用卡
     */
    CREDIT_CARD(3, "信用卡", "💳"),

    /**
     * 支付宝
     */
    ALIPAY(4, "支付宝", "📱"),

    /**
     * 微信
     */
    WECHAT(5, "微信", "💬"),

    /**
     * 其他
     */
    OTHER(6, "其他", "📦");

    private final Integer code;
    private final String name;
    private final String icon;

    AccountType(Integer code, String name, String icon) {
        this.code = code;
        this.name = name;
        this.icon = icon;
    }

    /**
     * 根据 code 获取枚举
     */
    public static AccountType fromCode(Integer code) {
        for (AccountType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + code);
    }
}
