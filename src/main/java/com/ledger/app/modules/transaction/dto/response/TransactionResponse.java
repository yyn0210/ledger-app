package com.ledger.app.modules.transaction.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易记录响应 DTO
 */
@Data
public class TransactionResponse {

    /**
     * 交易 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 交易类型：1=收入 2=支出 3=转账
     */
    private Integer type;

    /**
     * 交易类型名称
     */
    private String typeName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 账户 ID
     */
    private Long accountId;

    /**
     * 账户名称
     */
    private String accountName;

    /**
     * 目标账户 ID（转账时）
     */
    private Long toAccountId;

    /**
     * 目标账户名称（转账时）
     */
    private String toAccountName;

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
     * 标签
     */
    private List<String> tags;

    /**
     * 图片 URL
     */
    private List<String> imageUrls;

    /**
     * 是否转账
     */
    private Boolean isTransfer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
