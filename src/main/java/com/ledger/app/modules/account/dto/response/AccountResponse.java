package com.ledger.app.modules.account.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.app.modules.account.entity.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "账户响应")
public class AccountResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账户 ID", example = "1")
    private Long id;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "账户名称", example = "招商银行")
    private String name;

    @Schema(description = "账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他", example = "2")
    private Integer type;

    @Schema(description = "账户类型名称", example = "银行卡")
    private String typeName;

    @Schema(description = "图标 emoji", example = "🏦")
    private String icon;

    @Schema(description = "颜色", example = "#E10602")
    private String color;

    @Schema(description = "余额", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "货币代码", example = "CNY")
    private String currency;

    @Schema(description = "是否计入总资产", example = "1")
    private Integer isInclude;

    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;

    @Schema(description = "创建时间", example = "2026-03-24T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-03-24T11:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     */
    public static AccountResponse fromEntity(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .bookId(account.getBookId())
                .name(account.getName())
                .type(account.getType())
                .icon(account.getIcon())
                .color(account.getColor())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .isInclude(account.getIsInclude())
                .sortOrder(account.getSortOrder())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * 从实体转换（带类型名称）
     */
    public static AccountResponse fromEntityWithTypeName(Account account, String typeName) {
        AccountResponse response = fromEntity(account);
        response.setTypeName(typeName);
        return response;
    }
}
