package com.ledger.app.modules.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 账本响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {

    /**
     * 账本 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 账本名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 类型：1-普通账本，2-旅行账本，3-装修账本，4-婚礼账本等
     */
    private Integer type;

    /**
     * 是否默认账本：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     */
    public static BookResponse fromEntity(com.ledger.app.modules.book.entity.Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .userId(book.getUserId())
                .name(book.getName())
                .icon(book.getIcon())
                .color(book.getColor())
                .type(book.getType())
                .isDefault(book.getIsDefault())
                .sortOrder(book.getSortOrder())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
