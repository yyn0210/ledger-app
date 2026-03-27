package com.ledger.app.modules.recurring.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 周期账单状态枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum RecurringStatus {
    
    ACTIVE(1, "执行中"),
    PAUSED(2, "已暂停"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");
    
    @EnumValue
    private final Integer code;
    
    private final String name;
    
    RecurringStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    
    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    @JsonCreator
    public static RecurringStatus fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(status -> status.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid recurring status: " + code));
    }
}
