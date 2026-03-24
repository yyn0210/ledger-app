package com.ledger.app.modules.recurring.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

/**
 * 周期类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum RecurringType {
    
    DAILY(1, "每日", 86400),           // 每天执行
    WEEKLY(2, "每周", 604800),         // 每周执行
    BIWEEKLY(3, "每两周", 1209600),    // 每两周执行
    MONTHLY(4, "每月", 2592000),       // 每月执行
    QUARTERLY(5, "每季度", 7776000),   // 每季度执行
    YEARLY(6, "每年", 31536000);       // 每年执行
    
    @EnumValue
    private final Integer code;
    
    private final String name;
    
    private final Integer intervalSeconds;
    
    RecurringType(Integer code, String name, Integer intervalSeconds) {
        this.code = code;
        this.name = name;
        this.intervalSeconds = intervalSeconds;
    }
    
    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    @JsonCreator
    public static RecurringType fromCode(Integer code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid recurring type: " + code));
    }
    
    public static RecurringType fromName(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid recurring type: " + name));
    }
}
