package com.ledger.app.modules.transaction.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建转账交易请求 DTO
 */
@Data
public class TransferRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 转出账户 ID
     */
    @NotNull(message = "转出账户 ID 不能为空")
    private Long fromAccountId;

    /**
     * 转入账户 ID
     */
    @NotNull(message = "转入账户 ID 不能为空")
    private Long toAccountId;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
    private BigDecimal amount;

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
}
