package com.ledger.app.modules.budget.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预算实体类
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@TableName("budgets")
@Schema(description = "预算信息")
public class Budget implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "预算 ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "账本 ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "用户 ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "分类 ID（NULL 表示总预算）")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "预算名称")
    private String name;

    @Schema(description = "预算金额")
    private BigDecimal amount;

    @Schema(description = "周期：monthly/yearly/custom")
    private String period;

    @Schema(description = "开始日期")
    @TableField("start_date")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    @TableField("end_date")
    private LocalDate endDate;

    @Schema(description = "预警阈值（百分比）")
    @TableField("alert_threshold")
    private BigDecimal alertThreshold;

    @Schema(description = "状态：active/completed/overdue")
    private String status;

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
