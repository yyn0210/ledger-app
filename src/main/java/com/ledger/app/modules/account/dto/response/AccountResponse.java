package com.ledger.app.modules.account.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户响应 DTO
 */
@Data
public class AccountResponse {

    /**
     * 账户 ID
     */
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 账户名称
     */
    private String name;

    /**
     * 账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他
     */
    private Integer type;

    /**
     * 账户类型名称
     */
    private String typeName;

    /**
     * 账户类型图标
     */
    private String typeIcon;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 货币代码
     */
    private String currency;

    /**
     * 图标 emoji
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 是否计入总资产
     */
    private Boolean isInclude;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
