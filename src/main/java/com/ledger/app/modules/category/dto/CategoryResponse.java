package com.ledger.app.modules.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * 分类 ID
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
     * 图标
     */
    private String icon;

    /**
     * 类型：1-支出分类，2-收入分类
     */
    private Integer type;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否预置分类
     */
    private Integer isPreset;

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

    /**
     * 从实体转换
     */
    public static CategoryResponse fromEntity(com.ledger.app.modules.category.entity.Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .bookId(category.getBookId())
                .parentId(category.getParentId())
                .name(category.getName())
                .icon(category.getIcon())
                .type(category.getType())
                .sortOrder(category.getSortOrder())
                .isPreset(category.getIsPreset())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
