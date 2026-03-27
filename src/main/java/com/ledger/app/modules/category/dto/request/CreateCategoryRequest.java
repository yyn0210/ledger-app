package com.ledger.app.modules.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建分类请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "创建分类请求")
public class CreateCategoryRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @Schema(description = "父分类 ID，0 表示一级分类", example = "0")
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称", example = "餐饮", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "图标 emoji", example = "🍜")
    private String icon;

    @NotNull(message = "分类类型不能为空")
    @Schema(description = "分类类型：1=支出 2=收入", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder = 0;
}
