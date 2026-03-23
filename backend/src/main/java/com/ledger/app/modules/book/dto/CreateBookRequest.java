package com.ledger.app.modules.book.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建账本请求 DTO
 */
@Data
public class CreateBookRequest {

    /**
     * 账本名称
     */
    @NotBlank(message = "账本名称不能为空")
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
     * 类型：1-普通账本，2-旅行账本，3-装修账本，4-婚礼账本等
     */
    private Integer type = 1;

    /**
     * 是否设为默认账本
     */
    private Boolean setAsDefault = false;

    /**
     * 排序顺序
     */
    private Integer sortOrder = 0;
}
