package com.ledger.app.modules.recurring.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期账单实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recurring_bill")
public class RecurringBill implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 账单名称
     */
    @TableField("name")
    private String name;

    /**
     * 周期类型 (1-每日，2-每周，3-每两周，4-每月，5-每季度，6-每年)
     */
    @TableField("recurring_type")
    private Integer recurringType;

    /**
     * 周期值（如：每周的第几天，每月的第几天）
     */
    @TableField("recurring_value")
    private Integer recurringValue;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 分类 ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 账户 ID
     */
    @TableField("account_id")
    private Long accountId;

    /**
     * 交易类型 (1-收入，2-支出)
     */
    @TableField("transaction_type")
    private Integer transactionType;

    /**
     * 备注
     */
    @TableField("note")
    private String note;

    /**
     * 商户
     */
    @TableField("merchant")
    private String merchant;

    /**
     * 开始日期
     */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（可选，为空表示无限期）
     */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 下次执行日期
     */
    @TableField("next_execution_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextExecutionDate;

    /**
     * 上次执行日期
     */
    @TableField("last_execution_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastExecutionDate;

    /**
     * 已执行次数
     */
    @TableField("execution_count")
    @Builder.Default
    private Integer executionCount = 0;

    /**
     * 最大执行次数（可选，为空表示无限次）
     */
    @TableField("max_executions")
    private Integer maxExecutions;

    /**
     * 状态 (1-执行中，2-已暂停，3-已完成，4-已取消)
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否自动执行
     */
    @TableField("auto_execute")
    @Builder.Default
    private Boolean autoExecute = true;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
