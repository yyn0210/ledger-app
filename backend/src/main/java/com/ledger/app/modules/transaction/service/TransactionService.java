package com.ledger.app.modules.transaction.service;

import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;

/**
 * 交易服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface TransactionService {

    /**
     * 分页查询交易记录
     *
     * @param bookId 账本 ID
     * @param page 页码
     * @param size 每页数量
     * @param type 交易类型
     * @param categoryId 分类 ID
     * @param accountId 账户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param minAmount 最小金额
     * @param maxAmount 最大金额
     * @param keyword 关键词
     * @return 分页结果
     */
    TransactionPageResponse getTransactions(Long bookId, Integer page, Integer size,
                                            Integer type, Long categoryId, Long accountId,
                                            String startDate, String endDate,
                                            String minAmount, String maxAmount, String keyword);

    /**
     * 获取交易详情
     *
     * @param id 交易 ID
     * @param bookId 账本 ID
     * @return 交易详情
     */
    TransactionResponse getTransactionByIdAndBookId(Long id, Long bookId);

    /**
     * 创建交易记录
     *
     * @param userId 用户 ID
     * @param request 创建请求
     * @return 交易记录
     */
    TransactionResponse createTransaction(Long userId, CreateTransactionRequest request);

    /**
     * 更新交易记录
     *
     * @param id 交易 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 交易记录
     */
    TransactionResponse updateTransaction(Long id, Long bookId, Long userId, UpdateTransactionRequest request);

    /**
     * 删除交易记录
     *
     * @param id 交易 ID
     * @param bookId 账本 ID
     */
    void deleteTransaction(Long id, Long bookId);

    /**
     * 批量创建交易记录
     *
     * @param userId 用户 ID
     * @param request 批量创建请求
     * @return 创建结果
     */
    BatchCreateResponse batchCreate(Long userId, BatchCreateRequest request);

    /**
     * 批量删除交易记录
     *
     * @param bookId 账本 ID
     * @param request 批量删除请求
     * @return 删除结果
     */
    BatchDeleteResponse batchDelete(Long bookId, BatchDeleteRequest request);

    /**
     * 创建转账交易
     *
     * @param userId 用户 ID
     * @param request 转账请求
     * @return 交易记录
     */
    TransactionResponse createTransfer(Long userId, TransferRequest request);

    /**
     * 批量创建响应
     */
    record BatchCreateResponse(int createdCount, java.util.List<Long> ids) {}

    /**
     * 批量删除响应
     */
    record BatchDeleteResponse(int deletedCount) {}
}
