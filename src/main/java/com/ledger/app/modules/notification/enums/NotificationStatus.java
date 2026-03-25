package com.ledger.app.modules.notification.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 通知状态枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum NotificationStatus {

    PENDING(1, "待发送"),
    SENT(2, "已发送"),
    DELIVERED(3, "已送达"),
    READ(4, "已读"),
    FAILED(5, "发送失败");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String name;

    NotificationStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static NotificationStatus fromCode(Integer code) {
        for (NotificationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
