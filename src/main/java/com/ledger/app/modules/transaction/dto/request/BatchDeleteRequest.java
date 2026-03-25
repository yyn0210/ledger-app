package com.ledger.app.modules.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量删除交易记录请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "批量删除交易记录请求")
public class BatchDeleteRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotEmpty(message = "交易 ID 列表不能为空")
    @Schema(description = "交易 ID 列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> ids;
}
