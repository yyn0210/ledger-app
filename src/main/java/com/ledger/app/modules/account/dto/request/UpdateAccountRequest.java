package com.ledger.app.modules.account.dto.request;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 更新账户请求 DTO
 */
@Data
public class UpdateAccountRequest {

    /**
     * 账户名称
     */
    private String name;

    /**
     * 余额
     */
    @DecimalMin(value = "0.00", message = "余额不能为负数")
    private BigDecimal balance;

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
}
