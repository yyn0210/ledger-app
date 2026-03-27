package com.ledger.app.modules.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.app.modules.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "分类响应")
public class CategoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类 ID", example = "1")
    private Long id;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "父分类 ID", example = "0")
    private Long parentId;

    @Schema(description = "分类名称", example = "餐饮")
    private String name;

    @Schema(description = "图标 emoji", example = "🍜")
    private String icon;

    @Schema(description = "分类类型：1=支出 2=收入", example = "1")
    private Integer type;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否系统预设", example = "0")
    private Integer isSystem;

    @Schema(description = "子分类列表")
    private List<CategoryResponse> children;

    @Schema(description = "创建时间", example = "2026-03-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-03-24T11:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     */
    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .bookId(category.getBookId())
                .parentId(category.getParentId())
                .name(category.getName())
                .icon(category.getIcon())
                .type(category.getType())
                .sortOrder(category.getSortOrder())
                .isSystem(category.getIsSystem())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * 从实体转换（带子分类）
     */
    public static CategoryResponse fromEntityWithChildren(Category category, List<CategoryResponse> children) {
        CategoryResponse response = fromEntity(category);
        response.setChildren(children);
        return response;
    }
}
