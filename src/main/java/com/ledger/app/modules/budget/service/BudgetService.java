package com.ledger.app.modules.budget.service;

import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetAlertResponse;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;

import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService {

    /**
     * 获取预算列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期（可选）
     * @param status 状态（可选）
     * @return 预算列表
     */
    List<BudgetResponse> getBudgets(Long bookId, Long userId, String period, String status);

    /**
     * 获取预算详情（含执行进度）
     *
     * @param id     预算 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 预算详情
     */
    BudgetDetailResponse getBudgetDetail(Long id, Long bookId, Long userId);

    /**
     * 创建预算
     *
     * @param request 创建请求
     * @return 预算 ID
     */
    Long createBudget(CreateBudgetRequest request);

    /**
     * 更新预算
     *
     * @param id      预算 ID
     * @param request 更新请求
     * @param bookId  账本 ID
     * @param userId  用户 ID
     */
    void updateBudget(Long id, UpdateBudgetRequest request, Long bookId, Long userId);

    /**
     * 删除预算
     *
     * @param id     预算 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     */
    void deleteBudget(Long id, Long bookId, Long userId);

    /**
     * 获取预算汇总统计
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期（可选）
     * @return 预算汇总统计
     */
    BudgetSummaryResponse getBudgetSummary(Long bookId, Long userId, String period);

    /**
     * 检查预算预警
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 预算预警列表
     */
    List<BudgetAlertResponse> checkBudgetAlerts(Long bookId, Long userId);
}
