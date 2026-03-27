package com.ledger.app.modules.export.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 导出状态枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum ExportStatus {

    PENDING(1, "等待生成"),
    PROCESSING(2, "生成中"),
    COMPLETED(3, "已完成"),
    FAILED(4, "生成失败");

    @EnumValue
    private final Integer code;

    @JsonValue
    private final String name;

    ExportStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ExportStatus fromCode(Integer code) {
        for (ExportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PENDING;
    }
}
