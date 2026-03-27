package com.ledger.app.modules.budget.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预算详情响应（含执行进度和交易列表）
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "预算详情响应")
public class BudgetDetailResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "预算 ID", example = "1")
    private Long id;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "预算名称", example = "3 月餐饮预算")
    private String name;

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "餐饮")
    private String categoryName;

    @Schema(description = "预算金额", example = "2000.00")
    private BigDecimal amount;

    @Schema(description = "实际支出", example = "1500.00")
    private BigDecimal spentAmount;

    @Schema(description = "执行进度（百分比）", example = "75.00")
    private BigDecimal progress;

    @Schema(description = "预算周期", example = "monthly")
    private String period;

    @Schema(description = "开始日期", example = "2026-03-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-03-31")
    private LocalDate endDate;

    @Schema(description = "预警阈值（百分比）", example = "80.00")
    private BigDecimal alertThreshold;

    @Schema(description = "状态：active/completed/overdue", example = "active")
    private String status;

    @Schema(description = "相关交易列表")
    private List<TransactionResponse> transactions;

    @Schema(description = "创建时间", example = "2026-03-01T00:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-03-01T00:00:00")
    private LocalDateTime updatedAt;
}
