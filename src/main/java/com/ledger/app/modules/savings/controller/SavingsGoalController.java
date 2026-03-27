package com.ledger.app.modules.savings.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.savings.dto.request.CreateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.request.UpdateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.response.SavingsGoalResponse;
import com.ledger.app.modules.savings.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 储蓄目标控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "储蓄目标管理", description = "储蓄目标 CRUD 及进度跟踪接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/savings")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
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
     * 获取储蓄目标列表
     */
    @Operation(summary = "获取储蓄目标列表", description = "获取指定账本的储蓄目标列表（支持按状态过滤）")
    @GetMapping
    public Result<List<SavingsGoalResponse>> getSavingsGoals(
            @RequestParam Long bookId,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<SavingsGoalResponse> goals = savingsGoalService.getSavingsGoals(bookId, userId, status);
        return Result.success(goals);
    }

    /**
     * 获取储蓄目标详情
     */
    @Operation(summary = "获取储蓄目标详情", description = "根据 ID 获取储蓄目标详细信息（含进度）")
    @GetMapping("/{id}")
    public Result<SavingsGoalResponse> getSavingsGoal(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        SavingsGoalResponse goal = savingsGoalService.getSavingsGoalById(id, userId);
        return Result.success(goal);
    }

    /**
     * 创建储蓄目标
     */
    @Operation(summary = "创建储蓄目标", description = "创建一个新的储蓄目标")
    @PostMapping
    public Result<SavingsGoalResponse> createSavingsGoal(
            @Valid @RequestBody CreateSavingsGoalRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        SavingsGoalResponse goal = savingsGoalService.createSavingsGoal(userId, request);
        return Result.success(goal);
    }

    /**
     * 更新储蓄目标
     */
    @Operation(summary = "更新储蓄目标", description = "更新指定储蓄目标的信息")
    @PutMapping("/{id}")
    public Result<SavingsGoalResponse> updateSavingsGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        SavingsGoalResponse goal = savingsGoalService.updateSavingsGoal(id, userId, request);
        return Result.success(goal);
    }

    /**
     * 删除储蓄目标
     */
    @Operation(summary = "删除储蓄目标", description = "软删除指定储蓄目标")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSavingsGoal(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        savingsGoalService.deleteSavingsGoal(id, userId);
        return Result.success();
    }

    /**
     * 更新储蓄进度
     */
    @Operation(summary = "更新储蓄进度", description = "存入金额，更新储蓄进度")
    @PostMapping("/{id}/deposit")
    public Result<SavingsGoalResponse> updateProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        SavingsGoalResponse goal = savingsGoalService.updateProgress(id, userId, amount);
        return Result.success(goal);
    }

    /**
     * 完成储蓄目标
     */
    @Operation(summary = "完成储蓄目标", description = "手动标记储蓄目标为已完成")
    @PostMapping("/{id}/complete")
    public Result<Void> completeSavingsGoal(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        savingsGoalService.completeSavingsGoal(id, userId);
        return Result.success();
    }

    /**
     * 获取储蓄汇总统计
     */
    @Operation(summary = "获取储蓄汇总统计", description = "获取储蓄汇总统计（总储蓄/目标数量等）")
    @GetMapping("/summary")
    public Result<SavingsGoalService.SavingsSummary> getSummary(
            @RequestParam Long bookId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        SavingsGoalService.SavingsSummary summary = savingsGoalService.getSummary(bookId, userId);
        return Result.success(summary);
    }

    /**
     * 获取即将到期的储蓄目标
     */
    @Operation(summary = "获取即将到期的储蓄目标", description = "获取 7 天内到期的储蓄目标（提醒）")
    @GetMapping("/expiring")
    public Result<List<SavingsGoalResponse>> getExpiringGoals(
            @RequestParam Long bookId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<SavingsGoalResponse> goals = savingsGoalService.getExpiringGoals(bookId, userId);
        return Result.success(goals);
    }
}
