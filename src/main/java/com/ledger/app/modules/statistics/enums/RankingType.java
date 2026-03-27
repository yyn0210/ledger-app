package com.ledger.app.modules.statistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排行榜类型枚举
 */
@Getter
@AllArgsConstructor
public enum RankingType {

    EXPENSE("expense", "支出排行榜"),
    INCOME("income", "收入排行榜");

    private final String code;
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 排行榜类型
     */
    public static RankingType fromCode(String code) {
        for (RankingType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ranking type: " + code);
    }
}
