package com.ledger.app.modules.account.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户汇总统计响应 DTO
 */
@Data
public class AccountSummaryResponse {

    /**
     * 总余额
     */
    private BigDecimal totalBalance;

    /**
     * 总收入（可选，从交易统计）
     */
    private BigDecimal totalIncome;

    /**
     * 总支出（可选，从交易统计）
     */
    private BigDecimal totalExpense;

    /**
     * 账户数量
     */
    private Integer accountCount;

    /**
     * 按类型分组的余额
     */
    private List<AccountTypeBalance> byType;

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
         * 类型图标
         */
        private String typeIcon;

        /**
         * 余额
         */
        private BigDecimal balance;
    }
}
