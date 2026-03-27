package com.ledger.app.modules.category.enums;

/**
 * 分类类型枚举
 */
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

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 类型码
     * @return 分类类型
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
