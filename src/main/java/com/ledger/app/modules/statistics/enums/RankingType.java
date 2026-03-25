package com.ledger.app.modules.statistics.enums;

import lombok.Getter;

/**
 * 排行榜类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum RankingType {

    /**
     * 支出排行榜
     */
    EXPENSE("expense", "支出排行榜"),

    /**
     * 收入排行榜
     */
    INCOME("income", "收入排行榜");

    private final String code;
    private final String description;

    RankingType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static RankingType fromCode(String code) {
        for (RankingType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return EXPENSE; // 默认支出排行榜
    }
}
