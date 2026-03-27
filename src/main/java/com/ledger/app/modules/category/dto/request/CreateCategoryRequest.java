package com.ledger.app.modules.category.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
     * 父分类 ID，0 表示一级分类
     */
    @NotNull(message = "父分类 ID 不能为空")
    private Long parentId;

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
     * 类型：1=支出 2=收入
     */
    @NotNull(message = "分类类型不能为空")
    private Integer type;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
