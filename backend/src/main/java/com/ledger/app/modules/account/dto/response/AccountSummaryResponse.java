package com.ledger.app.modules.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 账户汇总统计响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@Schema(description = "账户汇总统计响应")
public class AccountSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总资产", example = "50000.00")
    private BigDecimal totalBalance;

    @Schema(description = "总收入", example = "10000.00")
    private BigDecimal totalIncome;

    @Schema(description = "总支出", example = "5000.00")
    private BigDecimal totalExpense;

    @Schema(description = "账户数量", example = "5")
    private Integer accountCount;

    @Schema(description = "按类型分组统计")
    private List<TypeBalance> byType;

    /**
     * 类型余额
     */
    @Data
    @Builder
    @Schema(description = "类型余额")
    public static class TypeBalance implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "账户类型", example = "2")
        private Integer type;

        @Schema(description = "类型名称", example = "银行卡")
        private String typeName;

        @Schema(description = "余额", example = "48000.00")
        private BigDecimal balance;
    }
}
