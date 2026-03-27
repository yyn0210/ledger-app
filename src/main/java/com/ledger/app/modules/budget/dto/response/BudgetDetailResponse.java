package com.ledger.app.modules.budget.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预算详情响应 DTO（含执行进度和交易记录）
 */
@Data
public class BudgetDetailResponse {

    /**
     * 预算 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 预算名称
     */
    private String name;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 预算金额
     */
    private BigDecimal amount;

    /**
     * 已支出金额
     */
    private BigDecimal spentAmount;

    /**
     * 进度百分比
     */
    private BigDecimal progress;

    /**
     * 周期：monthly/yearly/custom
     */
    private String period;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 预警阈值（百分比）
     */
    private BigDecimal alertThreshold;

    /**
     * 状态：active/completed/overdue
     */
    private String status;

    /**
     * 相关交易记录
     */
    private List<TransactionInfo> transactions;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 交易信息 DTO
     */
    @Data
    public static class TransactionInfo {
        private Long id;
        private BigDecimal amount;
        private String title;
        private LocalDate transactionDate;
    }
}
