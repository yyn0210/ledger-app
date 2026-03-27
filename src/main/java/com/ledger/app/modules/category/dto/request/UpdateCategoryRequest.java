package com.ledger.app.modules.category.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 更新分类请求 DTO
 */
@Data
public class UpdateCategoryRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 图标 emoji
     */
    private String icon;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
