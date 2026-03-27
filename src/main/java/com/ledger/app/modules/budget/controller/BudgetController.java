package com.ledger.app.modules.budget.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;
import com.ledger.app.modules.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "预算管理", description = "预算 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
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
     * 获取预算列表
     */
    @Operation(summary = "获取预算列表", description = "获取指定账本的预算列表（支持按周期/状态过滤）")
    @GetMapping
    public Result<List<BudgetResponse>> getBudgets(
            @RequestParam Long bookId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status) {
        List<BudgetResponse> budgets = budgetService.getBudgets(bookId, period, status);
        return Result.success(budgets);
    }

    /**
     * 获取预算详情
     */
    @Operation(summary = "获取预算详情", description = "根据 ID 获取预算详细信息（含执行进度）")
    @GetMapping("/{id}")
    public Result<BudgetDetailResponse> getBudgetDetail(@PathVariable Long id, @RequestParam Long bookId) {
        BudgetDetailResponse detail = budgetService.getBudgetDetail(id, bookId);
        return Result.success(detail);
    }

    /**
     * 创建预算
     */
    @Operation(summary = "创建预算", description = "创建一个新的预算")
    @PostMapping
    public Result<BudgetResponse> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        BudgetResponse budget = budgetService.createBudget(userId, request);
        return Result.success(budget);
    }

    /**
     * 更新预算
     */
    @Operation(summary = "更新预算", description = "更新指定预算的信息")
    @PutMapping("/{id}")
    public Result<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @Valid @RequestBody UpdateBudgetRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        BudgetResponse budget = budgetService.updateBudget(id, bookId, userId, request);
        return Result.success(budget);
    }

    /**
     * 删除预算
     */
    @Operation(summary = "删除预算", description = "软删除指定预算")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBudget(@PathVariable Long id, @RequestParam Long bookId) {
        budgetService.deleteBudget(id, bookId);
        return Result.success(null);
    }

    /**
     * 获取预算汇总统计
     */
    @Operation(summary = "获取预算汇总统计", description = "获取预算汇总统计（总预算/总支出/按分类分组）")
    @GetMapping("/summary")
    public Result<BudgetSummaryResponse> getBudgetSummary(
            @RequestParam Long bookId,
            @RequestParam(required = false) String period) {
        BudgetSummaryResponse summary = budgetService.getBudgetSummary(bookId, period);
        return Result.success(summary);
    }

    /**
     * 检查预算预警
     */
    @Operation(summary = "检查预算预警", description = "获取所有超支或达到预警线的预算")
    @GetMapping("/alerts")
    public Result<List<BudgetResponse>> getBudgetAlerts(@RequestParam Long bookId) {
        List<BudgetResponse> alerts = budgetService.getBudgetAlerts(bookId);
        return Result.success(alerts);
    }
}
