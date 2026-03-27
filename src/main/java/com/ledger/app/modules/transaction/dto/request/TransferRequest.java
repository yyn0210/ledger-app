package com.ledger.app.modules.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建转账交易请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "创建转账交易请求")
public class TransferRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotNull(message = "转出账户 ID 不能为空")
    @Schema(description = "转出账户 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fromAccountId;

    @NotNull(message = "转入账户 ID 不能为空")
    @Schema(description = "转入账户 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toAccountId;

    @NotNull(message = "金额不能为空")
    @Schema(description = "金额", example = "1000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题", example = "银行卡转入微信", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "描述", example = "生活费转账")
    private String description;

    @NotNull(message = "交易日期不能为空")
    @Schema(description = "交易日期", example = "2026-03-24", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate transactionDate;
}
