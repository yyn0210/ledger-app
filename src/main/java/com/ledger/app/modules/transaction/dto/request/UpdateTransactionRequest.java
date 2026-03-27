package com.ledger.app.modules.transaction.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新交易记录请求 DTO
 */
@Data
public class UpdateTransactionRequest {

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 交易类型
     */
    private Integer type;

    /**
     * 金额
     */
    @DecimalMin(value = "0.00", message = "金额不能为负数")
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
}
