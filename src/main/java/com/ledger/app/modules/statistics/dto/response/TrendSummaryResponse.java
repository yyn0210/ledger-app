package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 趋势分析汇总响应 DTO
 */
@Data
public class TrendSummaryResponse {

    /**
     * 趋势列表
     */
    private List<TrendResponse> trend;
}
