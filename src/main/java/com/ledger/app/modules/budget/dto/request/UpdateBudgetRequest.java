package com.ledger.app.modules.budget.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新预算请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "更新预算请求")
public class UpdateBudgetRequest {

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "预算名称", example = "3 月餐饮预算 - 调整后")
    private String name;

    @Schema(description = "预算金额", example = "2500.00")
    private BigDecimal amount;

    @Schema(description = "预警阈值（百分比）", example = "90.00")
    private BigDecimal alertThreshold;

    @Schema(description = "开始日期", example = "2026-03-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-03-31")
    private LocalDate endDate;
}
