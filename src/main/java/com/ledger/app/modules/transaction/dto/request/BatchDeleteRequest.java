package com.ledger.app.modules.transaction.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量删除交易记录请求 DTO
 */
@Data
public class BatchDeleteRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 交易 ID 列表
     */
    @NotEmpty(message = "交易 ID 列表不能为空")
    private List<Long> ids;
}
