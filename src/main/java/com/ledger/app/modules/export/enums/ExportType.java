package com.ledger.app.modules.export.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 导出文件类型枚举
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Getter
public enum ExportType {

    EXCEL("excel", "Excel (.xlsx)"),
    CSV("csv", "CSV (.csv)"),
    PDF("pdf", "PDF (.pdf)");

    @EnumValue
    private final String code;

    @JsonValue
    private final String name;

    ExportType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ExportType fromCode(String code) {
        for (ExportType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return EXCEL;
    }
}
