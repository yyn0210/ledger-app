package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 排行榜响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "排行榜响应")
public class RankingResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "排名", example = "1")
    private Integer rank;

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "餐饮")
    private String categoryName;

    @Schema(description = "图标", example = "🍜")
    private String icon;

    @Schema(description = "金额", example = "2000.00")
    private BigDecimal amount;

    @Schema(description = "交易笔数", example = "30")
    private Long transactionCount;

    @Schema(description = "百分比", example = "40.00")
    private BigDecimal percentage;
}
