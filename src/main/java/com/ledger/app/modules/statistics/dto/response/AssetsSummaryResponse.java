package com.ledger.app.modules.statistics.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产汇总响应 DTO
 */
@Data
public class AssetsSummaryResponse {

    /**
     * 总资产
     */
    private BigDecimal totalAssets;

    /**
     * 总负债
     */
    private BigDecimal totalLiabilities;

    /**
     * 净资产
     */
    private BigDecimal netAssets;

    /**
     * 按账户类型分组
     */
    private List<AccountTypeBalance> byAccountType;

    /**
     * 账户数量
     */
    private Integer accountCount;

    /**
     * 账户类型余额 DTO
     */
    @Data
    public static class AccountTypeBalance {
        /**
         * 账户类型
         */
        private Integer type;

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 余额
         */
        private BigDecimal balance;
    }
}
