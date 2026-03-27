package com.ledger.app.modules.savings.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 储蓄目标响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavingsGoalResponse {

    /**
     * 主键 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 目标名称
     */
    private String name;

    /**
     * 目标金额
     */
    private BigDecimal targetAmount;

    /**
     * 已存金额
     */
    private BigDecimal savedAmount;

    /**
     * 进度百分比
     */
    private BigDecimal progress;

    /**
     * 目标日期
     */
    private LocalDate targetDate;

    /**
     * 每月存入金额
     */
    private BigDecimal monthlyAmount;

    /**
     * 关联账户 ID
     */
    private Long accountId;

    /**
     * 状态：1-进行中 2-已完成 3-已暂停 4-已取消
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
