package com.ledger.app.modules.statistics.service;

import com.ledger.app.modules.statistics.dto.response.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 支出统计（按分类分组）
     *
     * @param bookId    账本 ID
     * @param userId    用户 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类统计汇总
     */
    CategoryStatisticsSummaryResponse getExpenseByCategory(Long bookId, Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 收入统计（按分类分组）
     *
     * @param bookId    账本 ID
     * @param userId    用户 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 分类统计汇总
     */
    CategoryStatisticsSummaryResponse getIncomeByCategory(Long bookId, Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 收支趋势分析
     *
     * @param bookId    账本 ID
     * @param userId    用户 ID
     * @param type      趋势类型
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 趋势分析汇总
     */
    TrendSummaryResponse getTrend(Long bookId, Long userId, String type, LocalDate startDate, LocalDate endDate);

    /**
     * 收支排行榜
     *
     * @param bookId    账本 ID
     * @param userId    用户 ID
     * @param type      排行榜类型
     * @param limit     返回数量
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 排行榜汇总
     */
    RankingSummaryResponse getRanking(Long bookId, Long userId, String type, Integer limit, LocalDate startDate, LocalDate endDate);

    /**
     * 资产汇总统计
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 资产汇总
     */
    AssetsSummaryResponse getAssetsSummary(Long bookId, Long userId);

    /**
     * 月度收支概览
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param year   年份
     * @param month  月份
     * @return 月度概览
     */
    MonthlySummaryResponse getMonthlySummary(Long bookId, Long userId, Integer year, Integer month);

    /**
     * 年度统计概览
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param year   年份
     * @return 年度概览
     */
    YearlySummaryResponse getYearlySummary(Long bookId, Long userId, Integer year);

    /**
     * 预算执行对比
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param year   年份
     * @param month  月份
     * @return 预算执行对比
     */
    BudgetExecutionResponse getBudgetExecution(Long bookId, Long userId, Integer year, Integer month);
}
