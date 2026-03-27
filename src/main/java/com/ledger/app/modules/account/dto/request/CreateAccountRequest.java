package com.ledger.app.modules.account.dto.request;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建账户请求 DTO
 */
@Data
public class CreateAccountRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 用户 ID（从 JWT 解析）
     */
    private Long userId;

    /**
     * 账户名称
     */
    @NotBlank(message = "账户名称不能为空")
    private String name;

    /**
     * 账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他
     */
    @NotNull(message = "账户类型不能为空")
    private Integer type;

    /**
     * 余额
     */
    @DecimalMin(value = "0.00", message = "余额不能为负数")
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
}
