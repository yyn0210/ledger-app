package com.ledger.app.modules.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账本实体类
 */
@Data
@TableName("books")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账本 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
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
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
