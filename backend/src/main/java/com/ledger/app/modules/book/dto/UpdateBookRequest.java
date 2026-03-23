package com.ledger.app.modules.book.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 更新账本请求 DTO
 */
@Data
public class UpdateBookRequest {

    /**
     * 账本名称（可选）
     */
    @Size(max = 50, message = "账本名称长度不能超过 50")
    private String name;

    /**
     * 图标（可选）
     */
    private String icon;

    /**
     * 颜色（可选）
     */
    private String color;

    /**
     * 排序顺序（可选）
     */
    private Integer sortOrder;

    /**
     * 是否设为默认账本（可选）
     */
    private Boolean setAsDefault;
}
