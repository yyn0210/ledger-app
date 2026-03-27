package com.ledger.app.modules.budget.service;

import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;

import java.util.List;

/**
 * 预算服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface BudgetService {

    /**
     * 获取预算列表
     *
     * @param bookId 账本 ID
     * @param period 周期（null 表示全部）
     * @param status 状态（null 表示全部）
     * @return 预算列表
     */
    List<BudgetResponse> getBudgets(Long bookId, String period, String status);

    /**
     * 获取预算详情（含执行进度）
     *
     * @param id 预算 ID
     * @param bookId 账本 ID
     * @return 预算详情
     */
    BudgetDetailResponse getBudgetDetail(Long id, Long bookId);

    /**
     * 创建预算
     *
     * @param userId 用户 ID
     * @param request 创建请求
     * @return 预算信息
     */
    BudgetResponse createBudget(Long userId, CreateBudgetRequest request);

    /**
     * 更新预算
     *
     * @param id 预算 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 预算信息
     */
    BudgetResponse updateBudget(Long id, Long bookId, Long userId, UpdateBudgetRequest request);

    /**
     * 删除预算
     *
     * @param id 预算 ID
     * @param bookId 账本 ID
     */
    void deleteBudget(Long id, Long bookId);

    /**
     * 获取预算汇总统计
     *
     * @param bookId 账本 ID
     * @param period 周期（null 表示全部）
     * @return 汇总统计
     */
    BudgetSummaryResponse getBudgetSummary(Long bookId, String period);

    /**
     * 检查预算预警
     *
     * @param bookId 账本 ID
     * @return 预警列表
     */
    List<BudgetResponse> getBudgetAlerts(Long bookId);
}
