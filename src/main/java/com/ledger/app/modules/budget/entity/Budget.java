package com.ledger.app.modules.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预算实体类
 */
@Data
@TableName("budgets")
public class Budget {

    /**
     * 预算 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 分类 ID（NULL 表示总预算）
     */
    private Long categoryId;

    /**
     * 预算名称
     */
    private String name;

    /**
     * 预算金额
     */
    private BigDecimal amount;

    /**
     * 周期：monthly/yearly/custom
     */
    private String period;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 预警阈值（百分比）
     */
    private BigDecimal alertThreshold;

    /**
     * 状态：active/completed/overdue
     */
    private String status;

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
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableLogic
    private Integer deleted;
}
