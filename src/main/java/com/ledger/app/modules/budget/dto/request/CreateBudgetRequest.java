package com.ledger.app.modules.budget.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建预算请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "创建预算请求")
public class CreateBudgetRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotBlank(message = "预算名称不能为空")
    @Schema(description = "预算名称", example = "3 月餐饮预算", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "分类 ID（NULL 表示总预算）", example = "1")
    private Long categoryId;

    @NotNull(message = "预算金额不能为空")
    @Schema(description = "预算金额", example = "2000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "预算周期不能为空")
    @Schema(description = "预算周期：monthly/yearly/custom", example = "monthly", requiredMode = Schema.RequiredMode.REQUIRED)
    private String period;

    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期", example = "2026-03-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Schema(description = "结束日期", example = "2026-03-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;

    @Schema(description = "预警阈值（百分比）", example = "80.00")
    private BigDecimal alertThreshold = new BigDecimal("80.00");
}
