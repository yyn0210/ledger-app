package com.ledger.app.modules.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建账户请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "创建账户请求")
public class CreateAccountRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotBlank(message = "账户名称不能为空")
    @Schema(description = "账户名称", example = "招商银行", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "账户类型不能为空")
    @Schema(description = "账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "初始余额", example = "10000.00")
    private BigDecimal balance = BigDecimal.ZERO;

    @Schema(description = "货币代码", example = "CNY")
    private String currency = "CNY";

    @Schema(description = "图标 emoji", example = "🏦")
    private String icon;

    @Schema(description = "颜色", example = "#E10602")
    private String color;

    @Schema(description = "是否计入总资产", example = "true")
    private Integer isInclude = 1;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder = 0;
}
