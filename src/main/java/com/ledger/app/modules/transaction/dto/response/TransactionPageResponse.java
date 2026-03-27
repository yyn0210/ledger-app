package com.ledger.app.modules.transaction.dto.response;

import com.ledger.app.common.result.PageResult;
import lombok.Data;

/**
 * 交易记录分页响应 DTO
 */
@Data
public class TransactionPageResponse extends PageResult<TransactionResponse> {

    /**
     * 构造函数
     *
     * @param list       交易记录列表
     * @param total      总数
     * @param page       页码
     * @param size       每页数量
     * @param totalPages 总页数
     */
    public TransactionPageResponse(java.util.List<TransactionResponse> list, Long total, Integer page, Integer size, Integer totalPages) {
        super(list, total, page, size, totalPages);
    }
}
