package com.ledger.app.modules.transaction.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量创建交易记录请求 DTO
 */
@Data
public class BatchCreateRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 交易记录列表
     */
    @NotEmpty(message = "交易记录不能为空")
    private List<CreateTransactionRequest> transactions;
}
