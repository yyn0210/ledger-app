package com.ledger.app.modules.notification.service;

import com.ledger.app.modules.notification.dto.request.SaveTemplateRequest;
import com.ledger.app.modules.notification.dto.request.SendNotificationRequest;
import com.ledger.app.modules.notification.dto.request.UpdatePreferenceRequest;
import com.ledger.app.modules.notification.dto.response.NotificationResponse;
import com.ledger.app.modules.notification.dto.response.PreferenceResponse;
import com.ledger.app.modules.notification.dto.response.TemplateResponse;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface NotificationService {

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 通知响应
     */
    NotificationResponse sendNotification(SendNotificationRequest request);

    /**
     * 获取用户通知列表
     *
     * @param userId 用户 ID
     * @param limit 数量限制
     * @return 通知列表
     */
    List<NotificationResponse> getUserNotifications(Long userId, Integer limit);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户 ID
     * @return 未读数量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param id 通知 ID
     * @param userId 用户 ID
     */
    void markAsRead(Long id, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户 ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     *
     * @param id 通知 ID
     * @param userId 用户 ID
     */
    void deleteNotification(Long id, Long userId);

    /**
     * 获取用户通知偏好
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID（可选）
     * @return 偏好设置
     */
    PreferenceResponse getPreference(Long userId, Long bookId);

    /**
     * 更新用户通知偏好
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID（可选）
     * @param request 更新请求
     * @return 偏好设置
     */
    PreferenceResponse updatePreference(Long userId, Long bookId, UpdatePreferenceRequest request);

    /**
     * 保存通知模板
     *
     * @param request 保存请求
     * @return 模板响应
     */
    TemplateResponse saveTemplate(SaveTemplateRequest request);

    /**
     * 获取模板列表
     *
     * @return 模板列表
     */
    List<TemplateResponse> getTemplates();

    /**
     * 根据编码获取模板
     *
     * @param code 模板编码
     * @return 模板响应
     */
    TemplateResponse getTemplateByCode(String code);

    /**
     * 删除模板
     *
     * @param id 模板 ID
     */
    void deleteTemplate(Long id);

    /**
     * 发送预算预警通知
     *
     * @param userId 用户 ID
     * @param budgetName 预算名称
     * @param progress 进度百分比
     */
    void sendBudgetAlert(Long userId, String budgetName, java.math.BigDecimal progress);

    /**
     * 发送周期账单执行通知
     *
     * @param userId 用户 ID
     * @param billName 账单名称
     * @param amount 金额
     */
    void sendRecurringBillExecuted(Long userId, String billName, java.math.BigDecimal amount);
}
