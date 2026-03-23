package com.ledger.app.modules.category.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 */
@Data
@TableName("categories")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账本 ID（0 表示预置分类）
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 父分类 ID（0 表示一级分类）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标（emoji 或 URL）
     */
    private String icon;

    /**
     * 类型：1-支出分类，2-收入分类
     */
    private Integer type;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否预置分类：0-否，1-是
     */
    @TableField("is_preset")
    private Integer isPreset;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;
}
