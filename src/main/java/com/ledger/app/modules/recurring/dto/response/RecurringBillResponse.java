package com.ledger.app.modules.recurring.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期账单响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBillResponse {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 账单名称
     */
    private String name;

    /**
     * 周期类型名称
     */
    private String recurringTypeName;

    /**
     * 周期值
     */
    private Integer recurringValue;

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
     * 交易类型 (1-收入，2-支出)
     */
    private Integer transactionType;

    /**
     * 备注
     */
    private String note;

    /**
     * 商户
     */
    private String merchant;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 下次执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextExecutionDate;

    /**
     * 上次执行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastExecutionDate;

    /**
     * 已执行次数
     */
    private Integer executionCount;

    /**
     * 最大执行次数
     */
    private Integer maxExecutions;

    /**
     * 状态 (1-执行中，2-已暂停，3-已完成，4-已取消)
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 是否自动执行
     */
    private Boolean autoExecute;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
