package com.ledger.app.modules.category.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类响应 DTO
 */
@Data
public class CategoryResponse {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 父分类 ID
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标 emoji
     */
    private String icon;

    /**
     * 类型：1=支出 2=收入
     */
    private Integer type;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否系统预设分类
     */
    private Integer isSystem;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 子分类列表
     */
    private List<CategoryResponse> children;
}
