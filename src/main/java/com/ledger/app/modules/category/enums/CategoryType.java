package com.ledger.app.modules.category.enums;

import lombok.Getter;

/**
 * 分类类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum CategoryType {

    /**
     * 支出
     */
    EXPENSE(1, "支出"),

    /**
     * 收入
     */
    INCOME(2, "收入");

    private final Integer code;
    private final String description;

    CategoryType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static CategoryType fromCode(Integer code) {
        for (CategoryType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid category type: " + code);
    }
}
