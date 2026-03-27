package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 排行榜响应 DTO
 */
@Data
public class RankingSummaryResponse {

    /**
     * 排行榜类型
     */
    private String type;

    /**
     * 排行榜列表
     */
    private List<RankingResponse> ranking;
}
