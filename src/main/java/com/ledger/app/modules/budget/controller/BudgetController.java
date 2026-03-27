package com.ledger.app.modules.budget.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetAlertResponse;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;
import com.ledger.app.modules.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 预算管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 获取预算列表
     * GET /api/budgets?bookId={bookId}&period=monthly&status=active
     */
    @GetMapping
    public Result<List<BudgetResponse>> getBudgets(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "status", required = false) String status,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取预算列表，bookId: {}, period: {}, status: {}", bookId, period, status);

        Long effectiveUserId = userId != null ? userId : bookId;

        List<BudgetResponse> budgets = budgetService.getBudgets(bookId, effectiveUserId, period, status);
        return Result.success(budgets);
    }

    /**
     * 获取预算详情
     * GET /api/budgets/{id}
     */
    @GetMapping("/{id}")
    public Result<BudgetDetailResponse> getBudgetDetail(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取预算详情，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        BudgetDetailResponse budget = budgetService.getBudgetDetail(id, bookId, effectiveUserId);
        return Result.success(budget);
    }

    /**
     * 创建预算
     * POST /api/budgets
     */
    @PostMapping
    public Result<Long> createBudget(
            @RequestBody @Validated CreateBudgetRequest request,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("创建预算，request: {}", request);

        if (userId != null) {
            request.setUserId(userId);
        } else if (request.getUserId() == null) {
            request.setUserId(request.getBookId());
        }

        Long budgetId = budgetService.createBudget(request);
        return Result.success(budgetId);
    }

    /**
     * 更新预算
     * PUT /api/budgets/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateBudget(
            @PathVariable Long id,
            @RequestBody @Validated UpdateBudgetRequest request,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("更新预算，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        budgetService.updateBudget(id, request, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 删除预算
     * DELETE /api/budgets/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBudget(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("删除预算，id: {}, bookId: {}", id, bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        budgetService.deleteBudget(id, bookId, effectiveUserId);
        return Result.success(null);
    }

    /**
     * 获取预算汇总统计
     * GET /api/budgets/summary?bookId={bookId}&period=monthly
     */
    @GetMapping("/summary")
    public Result<BudgetSummaryResponse> getBudgetSummary(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(value = "period", required = false) String period,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("获取预算汇总统计，bookId: {}, period: {}", bookId, period);

        Long effectiveUserId = userId != null ? userId : bookId;

        BudgetSummaryResponse summary = budgetService.getBudgetSummary(bookId, effectiveUserId, period);
        return Result.success(summary);
    }

    /**
     * 检查预算预警
     * GET /api/budgets/alerts?bookId={bookId}
     */
    @GetMapping("/alerts")
    public Result<List<BudgetAlertResponse>> checkBudgetAlerts(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("检查预算预警，bookId: {}", bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        List<BudgetAlertResponse> alerts = budgetService.checkBudgetAlerts(bookId, effectiveUserId);
        return Result.success(alerts);
    }
}
