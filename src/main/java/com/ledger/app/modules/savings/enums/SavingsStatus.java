package com.ledger.app.modules.savings.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 储蓄目标状态枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum SavingsStatus {

    ACTIVE(1, "进行中"),
    COMPLETED(2, "已完成"),
    PAUSED(3, "已暂停"),
    CANCELLED(4, "已取消");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String name;

    SavingsStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static SavingsStatus fromCode(Integer code) {
        for (SavingsStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
