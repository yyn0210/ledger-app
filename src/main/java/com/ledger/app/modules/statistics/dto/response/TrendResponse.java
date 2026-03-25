package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 趋势分析响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "趋势分析响应")
public class TrendResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "统计周期", example = "2026-03")
    private String period;

    @Schema(description = "收入金额", example = "10000.00")
    private BigDecimal income;

    @Schema(description = "支出金额", example = "6000.00")
    private BigDecimal expense;

    @Schema(description = "结余", example = "4000.00")
    private BigDecimal surplus;
}
