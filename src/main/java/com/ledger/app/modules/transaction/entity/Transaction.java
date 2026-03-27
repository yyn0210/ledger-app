package com.ledger.app.modules.transaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易记录实体类
 */
@Data
@TableName("transactions")
public class Transaction {

    /**
     * 交易 ID
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
     * 交易类型：1=收入 2=支出 3=转账
     */
    private Integer type;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 账户 ID
     */
    private Long accountId;

    /**
     * 目标账户 ID（转账时）
     */
    private Long toAccountId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 交易日期
     */
    private LocalDate transactionDate;

    /**
     * 地点
     */
    private String location;

    /**
     * 商户
     */
    private String merchant;

    /**
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 图片 URL（JSON 数组）
     */
    private String imageUrls;

    /**
     * 是否转账
     */
    private Boolean isTransfer;

    /**
     * 关联的转账记录 ID
     */
    private Long transferToId;

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
