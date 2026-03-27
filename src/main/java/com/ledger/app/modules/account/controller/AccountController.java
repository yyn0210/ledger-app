package com.ledger.app.modules.account.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.dto.response.AccountSummaryResponse;
import com.ledger.app.modules.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 账户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取账户列表
     * GET /api/accounts?bookId={bookId}
     */
    @GetMapping
    public Result<List<AccountResponse>> getAccounts(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取账户列表，bookId: {}, userId: {}", bookId, userId);
        
        // 如果 userId 未从 JWT 解析，使用 bookId 作为默认值（临时方案）
        Long effectiveUserId = userId != null ? userId : bookId;
        
        List<AccountResponse> accounts = accountService.getAccounts(bookId, effectiveUserId);
        return Result.success(accounts);
    }

    /**
     * 获取账户详情
     * GET /api/accounts/{id}
     */
    @GetMapping("/{id}")
    public Result<AccountResponse> getAccount(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取账户详情，id: {}, bookId: {}", id, bookId);
        
        Long effectiveUserId = userId != null ? userId : bookId;
        
        AccountResponse account = accountService.getAccount(id, bookId, effectiveUserId);
        return Result.success(account);
    }

    /**
     * 创建账户
     * POST /api/accounts
     */
    @PostMapping
    public Result<Long> createAccount(
            @RequestBody @Validated CreateAccountRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("创建账户，request: {}", request);
        
        // 如果 userId 未从 JWT 解析，使用 bookId 作为默认值
        if (userId != null) {
            request.setUserId(userId);
        } else if (request.getUserId() == null) {
            request.setUserId(request.getBookId());
        }
        
        Long accountId = accountService.createAccount(request);
        return Result.success(accountId);
    }

    /**
     * 更新账户
     * PUT /api/accounts/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateAccount(
            @PathVariable Long id,
            @RequestBody @Validated UpdateAccountRequest request,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("更新账户，id: {}, bookId: {}", id, bookId);
        
        Long effectiveUserId = userId != null ? userId : bookId;
        
        accountService.updateAccount(id, request, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 删除账户
     * DELETE /api/accounts/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAccount(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("删除账户，id: {}, bookId: {}", id, bookId);
        
        Long effectiveUserId = userId != null ? userId : bookId;
        
        accountService.deleteAccount(id, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 获取账户汇总统计
     * GET /api/accounts/summary?bookId={bookId}
     */
    @GetMapping("/summary")
    public Result<AccountSummaryResponse> getAccountSummary(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取账户汇总统计，bookId: {}", bookId);
        
        Long effectiveUserId = userId != null ? userId : bookId;
        
        AccountSummaryResponse summary = accountService.getAccountSummary(effectiveUserId);
        return Result.success(summary);
    }
}
