package com.ledger.app.modules.statistics.enums;

import lombok.Getter;

/**
 * 趋势类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum TrendType {

    /**
     * 按日统计
     */
    DAILY("daily", "按日统计"),

    /**
     * 按周统计
     */
    WEEKLY("weekly", "按周统计"),

    /**
     * 按月统计
     */
    MONTHLY("monthly", "按月统计"),

    /**
     * 按年统计
     */
    YEARLY("yearly", "按年统计");

    private final String code;
    private final String description;

    TrendType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static TrendType fromCode(String code) {
        for (TrendType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return MONTHLY; // 默认按月统计
    }
}
