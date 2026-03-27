package com.ledger.app.modules.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新分类请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "更新分类请求")
public class UpdateCategoryRequest {

    @Schema(description = "分类名称", example = "美食")
    private String name;

    @Schema(description = "图标 emoji", example = "🍔")
    private String icon;

    @Schema(description = "排序顺序", example = "2")
    private Integer sortOrder;
}
