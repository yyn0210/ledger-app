package com.ledger.app.modules.savings.service;

import com.ledger.app.modules.savings.dto.request.CreateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.request.UpdateSavingsGoalRequest;
import com.ledger.app.modules.savings.dto.response.SavingsGoalResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 储蓄目标服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface SavingsGoalService {

    /**
     * 获取储蓄目标列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param status 状态（null 表示全部）
     * @return 储蓄目标列表
     */
    List<SavingsGoalResponse> getSavingsGoals(Long bookId, Long userId, String status);

    /**
     * 获取储蓄目标详情
     *
     * @param id 储蓄目标 ID
     * @param userId 用户 ID
     * @return 储蓄目标详情
     */
    SavingsGoalResponse getSavingsGoalById(Long id, Long userId);

    /**
     * 创建储蓄目标
     *
     * @param userId 用户 ID
     * @param request 创建请求
     * @return 储蓄目标信息
     */
    SavingsGoalResponse createSavingsGoal(Long userId, CreateSavingsGoalRequest request);

    /**
     * 更新储蓄目标
     *
     * @param id 储蓄目标 ID
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 储蓄目标信息
     */
    SavingsGoalResponse updateSavingsGoal(Long id, Long userId, UpdateSavingsGoalRequest request);

    /**
     * 删除储蓄目标
     *
     * @param id 储蓄目标 ID
     * @param userId 用户 ID
     */
    void deleteSavingsGoal(Long id, Long userId);

    /**
     * 更新储蓄进度
     *
     * @param id 储蓄目标 ID
     * @param userId 用户 ID
     * @param amount 存入金额
     * @return 储蓄目标信息
     */
    SavingsGoalResponse updateProgress(Long id, Long userId, BigDecimal amount);

    /**
     * 完成储蓄目标
     *
     * @param id 储蓄目标 ID
     * @param userId 用户 ID
     */
    void completeSavingsGoal(Long id, Long userId);

    /**
     * 获取储蓄汇总统计
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 汇总统计
     */
    SavingsSummary getSummary(Long bookId, Long userId);

    /**
     * 获取即将到期的储蓄目标（7 天内）
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 即将到期的储蓄目标列表
     */
    List<SavingsGoalResponse> getExpiringGoals(Long bookId, Long userId);

    /**
     * 储蓄汇总统计
     */
    interface SavingsSummary {
        BigDecimal getTotalSaved();
        Integer getTotalGoals();
        Integer getCompletedGoals();
        Integer getActiveGoals();
    }
}
