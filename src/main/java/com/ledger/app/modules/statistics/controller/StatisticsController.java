package com.ledger.app.modules.statistics.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.statistics.dto.response.*;
import com.ledger.app.modules.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "统计分析", description = "收支统计/趋势分析/排行榜/资产汇总")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
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
     * 支出统计（按分类分组）
     */
    @Operation(summary = "支出统计", description = "按分类分组统计支出")
    @GetMapping("/expense-by-category")
    public Result<List<CategoryStatisticsResponse>> getExpenseByCategory(
            @RequestParam Long bookId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<CategoryStatisticsResponse> result = statisticsService.getExpenseByCategory(bookId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收入统计（按分类分组）
     */
    @Operation(summary = "收入统计", description = "按分类分组统计收入")
    @GetMapping("/income-by-category")
    public Result<List<CategoryStatisticsResponse>> getIncomeByCategory(
            @RequestParam Long bookId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<CategoryStatisticsResponse> result = statisticsService.getIncomeByCategory(bookId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收支趋势分析
     */
    @Operation(summary = "收支趋势分析", description = "按日/周/月/年统计收支趋势")
    @GetMapping("/trend")
    public Result<List<TrendResponse>> getTrend(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "monthly") String type,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<TrendResponse> result = statisticsService.getTrend(bookId, type, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收支排行榜
     */
    @Operation(summary = "收支排行榜", description = "支出/收入 TOP10 排行榜")
    @GetMapping("/ranking")
    public Result<List<RankingResponse>> getRanking(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "expense") String type,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<RankingResponse> result = statisticsService.getRanking(bookId, type, limit, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 资产汇总统计
     */
    @Operation(summary = "资产汇总统计", description = "总资产/总负债/净资产/按账户类型分组")
    @GetMapping("/assets")
    public Result<AssetsSummaryResponse> getAssetsSummary(
            @RequestParam Long bookId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        AssetsSummaryResponse result = statisticsService.getAssetsSummary(bookId, userId);
        return Result.success(result);
    }

    /**
     * 月度收支概览
     */
    @Operation(summary = "月度收支概览", description = "月度收支汇总 + 预算执行对比")
    @GetMapping("/monthly-summary")
    public Result<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam Long bookId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        MonthlySummaryResponse result = statisticsService.getMonthlySummary(bookId, userId, year, month);
        return Result.success(result);
    }

    /**
     * 年度统计概览
     */
    @Operation(summary = "年度统计概览", description = "全年收支汇总 + 月均统计")
    @GetMapping("/yearly-summary")
    public Result<YearlySummaryResponse> getYearlySummary(
            @RequestParam Long bookId,
            @RequestParam Integer year,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        YearlySummaryResponse result = statisticsService.getYearlySummary(bookId, userId, year);
        return Result.success(result);
    }

    /**
     * 预算执行对比
     */
    @Operation(summary = "预算执行对比", description = "各预算项执行情况对比")
    @GetMapping("/budget-execution")
    public Result<BudgetExecutionResponse> getBudgetExecution(
            @RequestParam Long bookId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        BudgetExecutionResponse result = statisticsService.getBudgetExecution(bookId, userId, year, month);
        return Result.success(result);
    }
}
