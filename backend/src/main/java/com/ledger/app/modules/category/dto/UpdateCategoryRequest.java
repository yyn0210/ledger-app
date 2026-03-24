package com.ledger.app.modules.category.dto;

import lombok.Data;
import jakarta.validation.constraints.Size;

/**
 * 更新分类请求 DTO
 */
@Data
public class UpdateCategoryRequest {

    /**
     * 分类名称（可选）
     */
    @Size(max = 50, message = "分类名称长度不能超过 50")
    private String name;

    /**
     * 图标（可选）
     */
    private String icon;

    /**
     * 排序顺序（可选）
     */
    private Integer sortOrder;
}
