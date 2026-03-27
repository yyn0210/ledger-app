package com.ledger.app.modules.statistics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产汇总响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "资产汇总响应")
public class AssetsSummaryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总资产", example = "50000.00")
    private BigDecimal totalAssets;

    @Schema(description = "总负债", example = "10000.00")
    private BigDecimal totalLiabilities;

    @Schema(description = "净资产", example = "40000.00")
    private BigDecimal netAssets;

    @Schema(description = "账户数量", example = "5")
    private Integer accountCount;

    @Schema(description = "按账户类型分组")
    private List<AccountTypeBalance> byAccountType;

    /**
     * 账户类型余额
     */
    @Data
    @Builder
    @Schema(description = "账户类型余额")
    public static class AccountTypeBalance implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "账户类型", example = "2")
        private Integer type;

        @Schema(description = "类型名称", example = "银行卡")
        private String typeName;

        @Schema(description = "余额", example = "45000.00")
        private BigDecimal balance;
    }
}
