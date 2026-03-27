package com.ledger.app.modules.recurring.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建周期账单请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringBillRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 账单名称
     */
    @NotBlank(message = "账单名称不能为空")
    @Size(max = 50, message = "账单名称不能超过 50 个字符")
    private String name;

    /**
     * 周期类型 (1-每日，2-每周，3-每两周，4-每月，5-每季度，6-每年)
     */
    @NotNull(message = "周期类型不能为空")
    @Min(value = 1, message = "周期类型无效")
    @Max(value = 6, message = "周期类型无效")
    private Integer recurringType;

    /**
     * 周期值（如：每周的第几天，每月的第几天）
     */
    private Integer recurringValue;

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
    private Long accountId;

    /**
     * 交易类型 (1-收入，2-支出)
     */
    @NotNull(message = "交易类型不能为空")
    @Min(value = 1, message = "交易类型无效")
    @Max(value = 2, message = "交易类型无效")
    private Integer transactionType;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注不能超过 200 个字符")
    private String note;

    /**
     * 商户
     */
    @Size(max = 100, message = "商户名称不能超过 100 个字符")
    private String merchant;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 最大执行次数（可选）
     */
    @Min(value = 1, message = "最大执行次数必须大于 0")
    private Integer maxExecutions;

    /**
     * 是否自动执行
     */
    @Builder.Default
    private Boolean autoExecute = true;
}
