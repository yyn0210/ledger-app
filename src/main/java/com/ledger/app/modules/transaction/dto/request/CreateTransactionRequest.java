package com.ledger.app.modules.transaction.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建交易记录请求 DTO
 */
@Data
public class CreateTransactionRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 用户 ID（从 JWT 解析）
     */
    private Long userId;

    /**
     * 交易类型：1=收入 2=支出 3=转账
     */
    @NotNull(message = "交易类型不能为空")
    private Integer type;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

    /**
     * 分类 ID
     */
    @NotNull(message = "分类 ID 不能为空")
    private Long categoryId;

    /**
     * 账户 ID
     */
    @NotNull(message = "账户 ID 不能为空")
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
    @NotNull(message = "交易日期不能为空")
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
