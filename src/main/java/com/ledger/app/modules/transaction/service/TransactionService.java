package com.ledger.app.modules.transaction.service;

import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;

import java.util.List;

/**
 * 交易记录服务接口
 */
public interface TransactionService {

    /**
     * 分页查询交易记录
     *
     * @param bookId     账本 ID
     * @param userId     用户 ID
     * @param page       页码
     * @param size       每页数量
     * @param type       交易类型（可选）
     * @param categoryId 分类 ID（可选）
     * @param accountId  账户 ID（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @param minAmount  最小金额（可选）
     * @param maxAmount  最大金额（可选）
     * @param keyword    关键词（可选）
     * @return 分页结果
     */
    TransactionPageResponse getTransactions(
            Long bookId, Long userId,
            Integer page, Integer size,
            Integer type, Long categoryId, Long accountId,
            java.time.LocalDate startDate, java.time.LocalDate endDate,
            java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount,
            String keyword
    );

    /**
     * 获取交易详情
     *
     * @param id     交易 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 交易详情
     */
    TransactionResponse getTransaction(Long id, Long bookId, Long userId);

    /**
     * 创建交易记录
     *
     * @param request 创建请求
     * @return 交易 ID
     */
    Long createTransaction(CreateTransactionRequest request);

    /**
     * 更新交易记录
     *
     * @param id      交易 ID
     * @param request 更新请求
     * @param bookId  账本 ID
     * @param userId  用户 ID
     */
    void updateTransaction(Long id, UpdateTransactionRequest request, Long bookId, Long userId);

    /**
     * 删除交易记录
     *
     * @param id     交易 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     */
    void deleteTransaction(Long id, Long bookId, Long userId);

    /**
     * 批量创建交易记录
     *
     * @param request 批量创建请求
     * @return 创建结果（ID 列表）
     */
    List<Long> batchCreateTransactions(BatchCreateRequest request);

    /**
     * 批量删除交易记录
     *
     * @param request 批量删除请求
     * @return 删除数量
     */
    Integer batchDeleteTransactions(BatchDeleteRequest request);

    /**
     * 创建转账交易
     *
     * @param request 转账请求
     * @return 交易 ID
     */
    Long createTransfer(TransferRequest request);
}
