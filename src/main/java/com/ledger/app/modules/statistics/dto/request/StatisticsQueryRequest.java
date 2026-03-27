package com.ledger.app.modules.statistics.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统计查询请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "统计查询请求")
public class StatisticsQueryRequest {

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "开始日期", example = "2026-03-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2026-03-31")
    private String endDate;

    @Schema(description = "统计维度", example = "monthly")
    private String type;

    @Schema(description = "返回数量", example = "10")
    private Integer limit;

    @Schema(description = "年份", example = "2026")
    private Integer year;

    @Schema(description = "月份", example = "3")
    private Integer month;
}
