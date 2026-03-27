package com.ledger.app.modules.transaction.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;
import com.ledger.app.modules.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 分页查询交易记录
     * GET /api/transactions?bookId={bookId}&page=1&size=20&type=1&categoryId=1&accountId=1&startDate=2026-03-01&endDate=2026-03-31
     */
    @GetMapping
    public Result<TransactionPageResponse> getTransactions(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(value = "minAmount", required = false) BigDecimal minAmount,
            @RequestParam(value = "maxAmount", required = false) BigDecimal maxAmount,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("分页查询交易记录，bookId: {}, page: {}, size: {}", bookId, page, size);

        Long effectiveUserId = userId != null ? userId : bookId;

        TransactionPageResponse result = transactionService.getTransactions(
                bookId, effectiveUserId, page, size, type, categoryId, accountId,
                startDate, endDate, minAmount, maxAmount, keyword
        );
        return Result.success(result);
    }

    /**
     * 获取交易详情
     * GET /api/transactions/{id}
     */
    @GetMapping("/{id}")
    public Result<TransactionResponse> getTransaction(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取交易详情，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        TransactionResponse transaction = transactionService.getTransaction(id, bookId, effectiveUserId);
        return Result.success(transaction);
    }

    /**
     * 创建交易记录
     * POST /api/transactions
     */
    @PostMapping
    public Result<Long> createTransaction(
            @RequestBody @Validated CreateTransactionRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("创建交易记录，request: {}", request);

        if (userId != null) {
            request.setUserId(userId);
        } else if (request.getUserId() == null) {
            request.setUserId(request.getBookId());
        }

        Long transactionId = transactionService.createTransaction(request);
        return Result.success(transactionId);
    }

    /**
     * 更新交易记录
     * PUT /api/transactions/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateTransaction(
            @PathVariable Long id,
            @RequestBody @Validated UpdateTransactionRequest request,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("更新交易记录，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        transactionService.updateTransaction(id, request, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 删除交易记录
     * DELETE /api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTransaction(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("删除交易记录，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        transactionService.deleteTransaction(id, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 批量创建交易记录
     * POST /api/transactions/batch
     */
    @PostMapping("/batch")
    public Result<List<Long>> batchCreateTransactions(
            @RequestBody @Validated BatchCreateRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("批量创建交易记录，count: {}", request.getTransactions().size());

        if (userId != null) {
            request.setUserId(userId);
        }

        List<Long> ids = transactionService.batchCreateTransactions(request);
        return Result.success(ids);
    }

    /**
     * 批量删除交易记录
     * POST /api/transactions/batch-delete
     */
    @PostMapping("/batch-delete")
    public Result<Integer> batchDeleteTransactions(
            @RequestBody @Validated BatchDeleteRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("批量删除交易记录，count: {}", request.getIds().size());

        Integer count = transactionService.batchDeleteTransactions(request);
        return Result.success(count);
    }

    /**
     * 创建转账交易
     * POST /api/transactions/transfer
     */
    @PostMapping("/transfer")
    public Result<Long> createTransfer(
            @RequestBody @Validated TransferRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("创建转账交易，fromAccountId: {}, toAccountId: {}, amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        if (userId != null) {
            request.setUserId(userId);
        } else if (request.getUserId() == null) {
            request.setUserId(request.getBookId());
        }

        Long transactionId = transactionService.createTransfer(request);
        return Result.success(transactionId);
    }
}
