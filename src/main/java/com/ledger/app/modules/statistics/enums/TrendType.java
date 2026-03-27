package com.ledger.app.modules.statistics.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 趋势类型枚举
 */
@Getter
@AllArgsConstructor
public enum TrendType {

    DAILY("daily", "按日统计"),
    WEEKLY("weekly", "按周统计"),
    MONTHLY("monthly", "按月统计"),
    YEARLY("yearly", "按年统计");

    private final String code;
    private final String name;

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 趋势类型
     */
    public static TrendType fromCode(String code) {
        for (TrendType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid trend type: " + code);
    }
}
