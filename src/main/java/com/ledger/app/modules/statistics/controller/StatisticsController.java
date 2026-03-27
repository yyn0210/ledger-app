package com.ledger.app.modules.statistics.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.statistics.dto.response.*;
import com.ledger.app.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 统计分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 支出统计（按分类分组）
     * GET /api/statistics/expense-by-category?bookId={bookId}&startDate=2026-03-01&endDate=2026-03-31
     */
    @GetMapping("/expense-by-category")
    public Result<CategoryStatisticsSummaryResponse> getExpenseByCategory(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam @NotNull(message = "开始日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @NotNull(message = "结束日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("支出统计（按分类），bookId: {}, startDate: {}, endDate: {}", bookId, startDate, endDate);

        Long effectiveUserId = userId != null ? userId : bookId;

        CategoryStatisticsSummaryResponse result = statisticsService.getExpenseByCategory(bookId, effectiveUserId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收入统计（按分类分组）
     * GET /api/statistics/income-by-category?bookId={bookId}&startDate=2026-03-01&endDate=2026-03-31
     */
    @GetMapping("/income-by-category")
    public Result<CategoryStatisticsSummaryResponse> getIncomeByCategory(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam @NotNull(message = "开始日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @NotNull(message = "结束日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("收入统计（按分类），bookId: {}, startDate: {}, endDate: {}", bookId, startDate, endDate);

        Long effectiveUserId = userId != null ? userId : bookId;

        CategoryStatisticsSummaryResponse result = statisticsService.getIncomeByCategory(bookId, effectiveUserId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收支趋势分析
     * GET /api/statistics/trend?bookId={bookId}&type=monthly&startDate=2026-01-01&endDate=2026-03-31
     */
    @GetMapping("/trend")
    public Result<TrendSummaryResponse> getTrend(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(value = "type", defaultValue = "monthly") String type,
            @RequestParam @NotNull(message = "开始日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @NotNull(message = "结束日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("收支趋势分析，bookId: {}, type: {}", bookId, type);

        Long effectiveUserId = userId != null ? userId : bookId;

        TrendSummaryResponse result = statisticsService.getTrend(bookId, effectiveUserId, type, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 收支排行榜
     * GET /api/statistics/ranking?bookId={bookId}&type=expense&limit=10&startDate=2026-03-01&endDate=2026-03-31
     */
    @GetMapping("/ranking")
    public Result<RankingSummaryResponse> getRanking(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(value = "type", defaultValue = "expense") String type,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam @NotNull(message = "开始日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @NotNull(message = "结束日期不能为空") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("收支排行榜，bookId: {}, type: {}, limit: {}", bookId, type, limit);

        Long effectiveUserId = userId != null ? userId : bookId;

        RankingSummaryResponse result = statisticsService.getRanking(bookId, effectiveUserId, type, limit, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 资产汇总统计
     * GET /api/statistics/assets?bookId={bookId}
     */
    @GetMapping("/assets")
    public Result<AssetsSummaryResponse> getAssetsSummary(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("资产汇总统计，bookId: {}", bookId);

        Long effectiveUserId = userId != null ? userId : bookId;

        AssetsSummaryResponse result = statisticsService.getAssetsSummary(bookId, effectiveUserId);
        return Result.success(result);
    }

    /**
     * 月度收支概览
     * GET /api/statistics/monthly-summary?bookId={bookId}&year=2026&month=3
     */
    @GetMapping("/monthly-summary")
    public Result<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotNull(message = "月份不能为空") Integer month,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("月度收支概览，bookId: {}, year: {}, month: {}", bookId, year, month);

        Long effectiveUserId = userId != null ? userId : bookId;

        MonthlySummaryResponse result = statisticsService.getMonthlySummary(bookId, effectiveUserId, year, month);
        return Result.success(result);
    }

    /**
     * 年度统计概览
     * GET /api/statistics/yearly-summary?bookId={bookId}&year=2026
     */
    @GetMapping("/yearly-summary")
    public Result<YearlySummaryResponse> getYearlySummary(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("年度统计概览，bookId: {}, year: {}", bookId, year);

        Long effectiveUserId = userId != null ? userId : bookId;

        YearlySummaryResponse result = statisticsService.getYearlySummary(bookId, effectiveUserId, year);
        return Result.success(result);
    }

    /**
     * 预算执行对比
     * GET /api/statistics/budget-execution?bookId={bookId}&year=2026&month=3
     */
    @GetMapping("/budget-execution")
    public Result<BudgetExecutionResponse> getBudgetExecution(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam @NotNull(message = "年份不能为空") Integer year,
            @RequestParam @NotNull(message = "月份不能为空") Integer month,
            @RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("预算执行对比，bookId: {}, year: {}, month: {}", bookId, year, month);

        Long effectiveUserId = userId != null ? userId : bookId;

        BudgetExecutionResponse result = statisticsService.getBudgetExecution(bookId, effectiveUserId, year, month);
        return Result.success(result);
    }
}
