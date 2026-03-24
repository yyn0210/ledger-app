package com.ledger.app.modules.recurring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新周期账单请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringBillRequest {

    /**
     * 账单名称
     */
    @Size(max = 50, message = "账单名称不能超过 50 个字符")
    private String name;

    /**
     * 金额
     */
    @DecimalMin(value = "0.01", message = "金额必须大于 0")
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
     * 结束日期（可选）
     */
    private LocalDate endDate;

    /**
     * 最大执行次数（可选）
     */
    @Min(value = 1, message = "最大执行次数必须大于 0")
    private Integer maxExecutions;

    /**
     * 是否自动执行
     */
    private Boolean autoExecute;

    /**
     * 状态 (1-执行中，2-已暂停，3-已完成，4-已取消)
     */
    @Min(value = 1, message = "状态无效")
    @Max(value = 4, message = "状态无效")
    private Integer status;
}
