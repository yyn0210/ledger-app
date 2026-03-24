package com.ledger.app.modules.transaction.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 交易记录控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "交易记录管理", description = "交易记录 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    /**
     * 获取当前用户 ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return authService.getUserIdFromToken(token);
    }

    /**
     * 从请求头获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取交易列表
     */
    @Operation(summary = "获取交易列表", description = "分页查询交易记录（支持复杂筛选）")
    @GetMapping
    public Result<TransactionPageResponse> getTransactions(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount,
            @RequestParam(required = false) String keyword) {
        TransactionPageResponse result = transactionService.getTransactions(
                bookId, page, size, type, categoryId, accountId,
                startDate, endDate, minAmount, maxAmount, keyword);
        return Result.success(result);
    }

    /**
     * 获取交易详情
     */
    @Operation(summary = "获取交易详情", description = "根据 ID 获取交易详细信息")
    @GetMapping("/{id}")
    public Result<TransactionResponse> getTransaction(@PathVariable Long id, @RequestParam Long bookId) {
        TransactionResponse result = transactionService.getTransactionByIdAndBookId(id, bookId);
        return Result.success(result);
    }

    /**
     * 创建交易记录
     */
    @Operation(summary = "创建交易记录", description = "创建一条新的交易记录（收入/支出）")
    @PostMapping
    public Result<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionResponse result = transactionService.createTransaction(userId, request);
        return Result.success(result);
    }

    /**
     * 更新交易记录
     */
    @Operation(summary = "更新交易记录", description = "更新指定交易记录的信息")
    @PutMapping("/{id}")
    public Result<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @Valid @RequestBody UpdateTransactionRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionResponse result = transactionService.updateTransaction(id, bookId, userId, request);
        return Result.success(result);
    }

    /**
     * 删除交易记录
     */
    @Operation(summary = "删除交易记录", description = "软删除指定交易记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTransaction(@PathVariable Long id, @RequestParam Long bookId) {
        transactionService.deleteTransaction(id, bookId);
        return Result.success(null);
    }

    /**
     * 批量创建交易记录
     */
    @Operation(summary = "批量创建交易记录", description = "一次性创建多条交易记录")
    @PostMapping("/batch")
    public Result<TransactionService.BatchCreateResponse> batchCreate(
            @Valid @RequestBody BatchCreateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionService.BatchCreateResponse result = transactionService.batchCreate(userId, request);
        return Result.success(result);
    }

    /**
     * 批量删除交易记录
     */
    @Operation(summary = "批量删除交易记录", description = "一次性删除多条交易记录")
    @PostMapping("/batch-delete")
    public Result<TransactionService.BatchDeleteResponse> batchDelete(
            @Valid @RequestBody BatchDeleteRequest request) {
        TransactionService.BatchDeleteResponse result = transactionService.batchDelete(request.getBookId(), request);
        return Result.success(result);
    }

    /**
     * 创建转账交易
     */
    @Operation(summary = "创建转账交易", description = "创建账户间转账交易（同时更新两个账户余额）")
    @PostMapping("/transfer")
    public Result<TransactionResponse> createTransfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        TransactionResponse result = transactionService.createTransfer(userId, request);
        return Result.success(result);
    }
}
