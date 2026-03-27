package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 趋势分析响应 DTO
 */
@Data
public class TrendResponse {

    /**
     * 周期（格式根据类型：2026-03-24 / 2026-W12 / 2026-03 / 2026）
     */
    private String period;

    /**
     * 收入金额
     */
    private BigDecimal income;

    /**
     * 支出金额
     */
    private BigDecimal expense;

    /**
     * 结余（收入 - 支出）
     */
    private BigDecimal surplus;
}
