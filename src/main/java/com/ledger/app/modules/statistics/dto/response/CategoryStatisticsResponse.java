package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 分类统计响应 DTO
 */
@Data
public class CategoryStatisticsResponse {

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类图标
     */
    private String categoryIcon;

    /**
     * 总金额
     */
    private BigDecimal amount;

    /**
     * 百分比
     */
    private BigDecimal percentage;

    /**
     * 交易笔数
     */
    private Integer transactionCount;
}
