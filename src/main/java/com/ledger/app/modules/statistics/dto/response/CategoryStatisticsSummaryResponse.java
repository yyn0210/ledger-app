package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分类统计汇总响应 DTO
 */
@Data
public class CategoryStatisticsSummaryResponse {

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 分类统计列表
     */
    private List<CategoryStatisticsResponse> categories;
}
