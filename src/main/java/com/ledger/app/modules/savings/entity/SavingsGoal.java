package com.ledger.app.modules.savings.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 储蓄目标实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("savings_goal")
public class SavingsGoal {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账本 ID
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 目标名称
     */
    @TableField("name")
    private String name;

    /**
     * 目标金额
     */
    @TableField("target_amount")
    private BigDecimal targetAmount;

    /**
     * 已存金额
     */
    @TableField("saved_amount")
    private BigDecimal savedAmount;

    /**
     * 目标日期
     */
    @TableField("target_date")
    private LocalDate targetDate;

    /**
     * 每月存入金额
     */
    @TableField("monthly_amount")
    private BigDecimal monthlyAmount;

    /**
     * 关联账户 ID（可选）
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 状态：1-进行中 2-已完成 3-已暂停 4-已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("note")
    private String note;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
