package com.ledger.app.modules.category.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@TableName("categories")
@Schema(description = "分类信息")
public class Category implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类 ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "账本 ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "父分类 ID，0 表示一级分类")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "图标 emoji")
    private String icon;

    @Schema(description = "类型：1=支出 2=收入")
    private Integer type;

    @Schema(description = "排序顺序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "是否系统预设：0=否 1=是")
    @TableField("is_system")
    private Integer isSystem;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "逻辑删除：0=未删除 1=已删除")
    @TableLogic
    private Integer deleted;
}
