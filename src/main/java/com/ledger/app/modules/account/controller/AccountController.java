package com.ledger.app.modules.account.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.dto.response.AccountSummaryResponse;
import com.ledger.app.modules.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "账户管理", description = "账户 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取账户列表
     */
    @Operation(summary = "获取账户列表", description = "获取指定账本的所有账户")
    @GetMapping
    public Result<List<AccountResponse>> getAccounts(@RequestParam Long bookId) {
        List<AccountResponse> accounts = accountService.getAccountsByBookId(bookId);
        return Result.success(accounts);
    }

    /**
     * 获取账户详情
     */
    @Operation(summary = "获取账户详情", description = "根据 ID 获取账户详细信息")
    @GetMapping("/{id}")
    public Result<AccountResponse> getAccount(@PathVariable Long id, @RequestParam Long bookId) {
        AccountResponse account = accountService.getAccountByIdAndBookId(id, bookId);
        return Result.success(account);
    }

    /**
     * 创建账户
     */
    @Operation(summary = "创建账户", description = "创建一个新的账户")
    @PostMapping
    public Result<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request);
        return Result.success(account);
    }

    /**
     * 更新账户
     */
    @Operation(summary = "更新账户", description = "更新指定账户的信息")
    @PutMapping("/{id}")
    public Result<AccountResponse> updateAccount(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @Valid @RequestBody UpdateAccountRequest request) {
        AccountResponse account = accountService.updateAccount(id, bookId, request);
        return Result.success(account);
    }

    /**
     * 删除账户
     */
    @Operation(summary = "删除账户", description = "软删除指定账户（已被引用的账户不能删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAccount(@PathVariable Long id, @RequestParam Long bookId) {
        accountService.deleteAccount(id, bookId);
        return Result.success(null);
    }

    /**
     * 账户汇总统计
     */
    @Operation(summary = "账户汇总统计", description = "获取账户汇总统计（总资产/按类型分组）")
    @GetMapping("/summary")
    public Result<AccountSummaryResponse> getAccountSummary(
            @RequestParam Long bookId,
            @RequestParam Long userId) {
        AccountSummaryResponse summary = accountService.getAccountSummary(bookId, userId);
        return Result.success(summary);
    }
}
