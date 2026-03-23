package com.ledger.app.modules.category.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建分类请求 DTO
 */
@Data
public class CreateCategoryRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 父分类 ID（0 表示一级分类）
     */
    private Long parentId = 0L;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50")
    private String name;

    /**
     * 图标（可选）
     */
    private String icon;

    /**
     * 类型：1-支出分类，2-收入分类
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 排序顺序
     */
    private Integer sortOrder = 0;
}
