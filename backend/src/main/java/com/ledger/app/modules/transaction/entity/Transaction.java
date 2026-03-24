package com.ledger.app.modules.transaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@TableName("transactions")
@Schema(description = "交易记录信息")
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "交易 ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "账本 ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "用户 ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "交易类型：1=收入 2=支出 3=转账")
    private Integer type;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "分类 ID")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "账户 ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "目标账户 ID（转账时）")
    @TableField("to_account_id")
    private Long toAccountId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "交易日期")
    @TableField("transaction_date")
    private LocalDate transactionDate;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "商户")
    private String merchant;

    @Schema(description = "标签（JSON 数组）")
    private String tags;

    @Schema(description = "图片 URL（JSON 数组）")
    @TableField("image_urls")
    private String imageUrls;

    @Schema(description = "是否转账")
    @TableField("is_transfer")
    private Integer isTransfer;

    @Schema(description = "关联的转账记录 ID")
    @TableField("transfer_to_id")
    private Long transferToId;

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
