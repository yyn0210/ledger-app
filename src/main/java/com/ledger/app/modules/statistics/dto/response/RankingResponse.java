package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 排行榜项响应 DTO
 */
@Data
public class RankingResponse {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 交易笔数
     */
    private Integer transactionCount;

    /**
     * 百分比
     */
    private BigDecimal percentage;
}
