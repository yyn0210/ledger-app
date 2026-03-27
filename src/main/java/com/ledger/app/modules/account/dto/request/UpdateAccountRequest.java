package com.ledger.app.modules.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新账户请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "更新账户请求")
public class UpdateAccountRequest {

    @Schema(description = "账户名称", example = "招商银行 - 工资卡")
    private String name;

    @Schema(description = "余额", example = "15000.00")
    private BigDecimal balance;

    @Schema(description = "图标 emoji", example = "🏦")
    private String icon;

    @Schema(description = "颜色", example = "#E10602")
    private String color;

    @Schema(description = "是否计入总资产", example = "true")
    private Integer isInclude;

    @Schema(description = "排序顺序", example = "2")
    private Integer sortOrder;
}
