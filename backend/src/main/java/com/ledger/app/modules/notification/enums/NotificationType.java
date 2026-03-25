package com.ledger.app.modules.notification.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum NotificationType {

    EMAIL(1, "邮件"),
    SMS(2, "短信"),
    IN_APP(3, "应用内"),
    PUSH(4, "推送通知");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String name;

    NotificationType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static NotificationType fromCode(Integer code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return IN_APP;
    }
}
