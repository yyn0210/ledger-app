package com.ledger.app.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.notification.dto.request.SaveTemplateRequest;
import com.ledger.app.modules.notification.dto.request.SendNotificationRequest;
import com.ledger.app.modules.notification.dto.request.UpdatePreferenceRequest;
import com.ledger.app.modules.notification.dto.response.NotificationResponse;
import com.ledger.app.modules.notification.dto.response.PreferenceResponse;
import com.ledger.app.modules.notification.dto.response.TemplateResponse;
import com.ledger.app.modules.notification.entity.Notification;
import com.ledger.app.modules.notification.entity.NotificationTemplate;
import com.ledger.app.modules.notification.entity.UserNotificationPreference;
import com.ledger.app.modules.notification.enums.NotificationStatus;
import com.ledger.app.modules.notification.enums.NotificationType;
import com.ledger.app.modules.notification.repository.NotificationRepository;
import com.ledger.app.modules.notification.service.NotificationService;
import com.ledger.app.modules.websocket.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketMessageService webSocketMessageService;

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\w+)\\}");

    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        // 如果使用模板，渲染模板内容
        String title = request.getTitle();
        String content = request.getContent();

        if (request.getTemplateCode() != null) {
            NotificationTemplate template = notificationRepository.findByCode(request.getTemplateCode());
            if (template == null) {
                throw new BusinessException("通知模板不存在：" + request.getTemplateCode());
            }
            title = renderTemplate(template.getTitleTemplate(), request.getTemplateVars());
            content = renderTemplate(template.getContent(), request.getTemplateVars());
        }

        // 创建通知记录
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(title)
                .content(content)
                .status(NotificationStatus.PENDING.getCode())
                .bizType(request.getBizType())
                .bizId(request.getBizId())
                .deleted(0)
                .build();

        notificationRepository.insert(notification);

        // 异步发送通知
        asyncSendNotification(notification);

        return buildResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId, Integer limit) {
        List<Notification> notifications = notificationRepository.findByUserId(userId, limit != null ? limit : 20);
        return notifications.stream().map(this::buildResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = getNotificationById(id, userId);
        notification.setStatus(NotificationStatus.READ.getCode());
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.updateById(notification);
        log.info("标记通知为已读：notificationId={}, userId={}", id, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .ne(Notification::getStatus, NotificationStatus.READ.getCode())
                .eq(Notification::getDeleted, 0);

        List<Notification> notifications = notificationRepository.selectList(wrapper);
        for (Notification notification : notifications) {
            notification.setStatus(NotificationStatus.READ.getCode());
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.updateBatchById(notifications);
        log.info("标记所有通知为已读：userId={}, count={}", userId, notifications.size());
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = getNotificationById(id, userId);
        notificationRepository.deleteById(id);
        log.info("删除通知：notificationId={}, userId={}", id, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PreferenceResponse getPreference(Long userId, Long bookId) {
        UserNotificationPreference preference = notificationRepository.findPreference(userId, bookId);
        if (preference == null) {
            // 返回默认偏好
            return PreferenceResponse.builder()
                    .userId(userId)
                    .bookId(bookId)
                    .emailEnabled(false)
                    .smsEnabled(false)
                    .inAppEnabled(true)
                    .pushEnabled(true)
                    .subscribedTypes(new ArrayList<>())
                    .build();
        }
        return buildPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public PreferenceResponse updatePreference(Long userId, Long bookId, UpdatePreferenceRequest request) {
        UserNotificationPreference preference = notificationRepository.findPreference(userId, bookId);

        if (preference == null) {
            preference = UserNotificationPreference.builder()
                    .userId(userId)
                    .bookId(bookId)
                    .emailEnabled(request.getEmailEnabled() != null ? request.getEmailEnabled() : false)
                    .smsEnabled(request.getSmsEnabled() != null ? request.getSmsEnabled() : false)
                    .inAppEnabled(request.getInAppEnabled() != null ? request.getInAppEnabled() : true)
                    .pushEnabled(request.getPushEnabled() != null ? request.getPushEnabled() : true)
                    .deleted(0)
                    .build();
            notificationRepository.insertPreference(preference);
        } else {
            if (request.getEmailEnabled() != null) preference.setEmailEnabled(request.getEmailEnabled());
            if (request.getSmsEnabled() != null) preference.setSmsEnabled(request.getSmsEnabled());
            if (request.getInAppEnabled() != null) preference.setInAppEnabled(request.getInAppEnabled());
            if (request.getPushEnabled() != null) preference.setPushEnabled(request.getPushEnabled());
            notificationRepository.updatePreference(preference);
        }

        log.info("更新通知偏好：userId={}, bookId={}", userId, bookId);
        return buildPreferenceResponse(preference);
    }

    @Override
    @Transactional
    public TemplateResponse saveTemplate(SaveTemplateRequest request) {
        // 检查编码是否已存在
        NotificationTemplate existing = notificationRepository.findByCode(request.getCode());
        if (existing != null && !existing.getId().equals(request.getId())) {
            throw new BusinessException("模板编码已存在：" + request.getCode());
        }

        NotificationTemplate template;
        if (request.getId() != null) {
            template = notificationRepository.selectById(request.getId());
            if (template == null) {
                throw new BusinessException("模板不存在：" + request.getId());
            }
            template.setName(request.getName());
            template.setType(request.getType());
            template.setContent(request.getContent());
            template.setTitleTemplate(request.getTitleTemplate());
            template.setBizType(request.getBizType());
            template.setIsEnabled(request.getIsEnabled());
            notificationRepository.updateTemplate(template);
        } else {
            template = NotificationTemplate.builder()
                    .name(request.getName())
                    .code(request.getCode())
                    .type(request.getType())
                    .content(request.getContent())
                    .titleTemplate(request.getTitleTemplate())
                    .bizType(request.getBizType())
                    .isEnabled(request.getIsEnabled())
                    .deleted(0)
                    .build();
            notificationRepository.insertTemplate(template);
        }

        log.info("保存通知模板：templateId={}, code={}", template.getId(), request.getCode());
        return buildTemplateResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getTemplates() {
        List<NotificationTemplate> templates = notificationRepository.findAll();
        return templates.stream().map(this::buildTemplateResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse getTemplateByCode(String code) {
        NotificationTemplate template = notificationRepository.findByCode(code);
        if (template == null) {
            throw new BusinessException("通知模板不存在：" + code);
        }
        return buildTemplateResponse(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        notificationRepository.deleteById(id);
        log.info("删除通知模板：templateId={}", id);
    }

    @Override
    @Async
    public void sendBudgetAlert(Long userId, String budgetName, java.math.BigDecimal progress) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.IN_APP.getCode())
                .title("预算预警")
                .content("预算\"" + budgetName + "\"已达到" + progress + "%，请注意控制支出。")
                .bizType("budget")
                .build();
        sendNotification(request);
    }

    @Override
    @Async
    public void sendRecurringBillExecuted(Long userId, String billName, java.math.BigDecimal amount) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.IN_APP.getCode())
                .title("周期账单执行")
                .content("周期账单\"" + billName + "\"已执行，金额：" + amount + "元。")
                .bizType("recurring")
                .build();
        sendNotification(request);
    }

    /**
     * 异步发送通知
     */
    @Async
    public void asyncSendNotification(Notification notification) {
        try {
            // 根据类型发送通知
            NotificationType type = NotificationType.fromCode(notification.getType());
            switch (type) {
                case EMAIL:
                    sendEmail(notification);
                    break;
                case SMS:
                    sendSms(notification);
                    break;
                case IN_APP:
                case PUSH:
                    sendInApp(notification);
                    break;
            }

            // 更新状态为已发送
            notification.setStatus(NotificationStatus.SENT.getCode());
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.updateById(notification);
            log.info("发送通知成功：notificationId={}, type={}", notification.getId(), type);
        } catch (Exception e) {
            log.error("发送通知失败：notificationId={}, error={}", notification.getId(), e.getMessage());
            notification.setStatus(NotificationStatus.FAILED.getCode());
            notification.setErrorMessage(e.getMessage());
            notificationRepository.updateById(notification);
        }
    }

    /**
     * 发送邮件通知
     */
    private void sendEmail(Notification notification) {
        // TODO: 集成邮件发送服务
        log.info("发送邮件通知：userId={}, title={}", notification.getUserId(), notification.getTitle());
    }

    /**
     * 发送短信通知
     */
    private void sendSms(Notification notification) {
        // TODO: 集成短信发送服务
        log.info("发送短信通知：userId={}, content={}", notification.getUserId(), notification.getContent());
    }

    /**
     * 发送应用内通知
     */
    private void sendInApp(Notification notification) {
        // 通过 WebSocket 推送
        webSocketMessageService.sendSystemNotification(
                notification.getUserId(),
                notification.getTitle(),
                notification.getContent()
        );
        log.info("发送应用内通知：userId={}, title={}", notification.getUserId(), notification.getTitle());
    }

    /**
     * 渲染模板
     */
    private String renderTemplate(String template, Map<String, Object> vars) {
        if (template == null || vars == null) {
            return template;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = vars.get(varName);
            matcher.appendReplacement(sb, value != null ? value.toString() : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 获取通知实体（带权限验证）
     */
    private Notification getNotificationById(Long id, Long userId) {
        Notification notification = notificationRepository.selectById(id);
        if (notification == null || notification.getDeleted() != 0) {
            throw new BusinessException("通知不存在");
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该通知");
        }
        return notification;
    }

    /**
     * 构建通知响应
     */
    private NotificationResponse buildResponse(Notification notification) {
        NotificationType type = NotificationType.fromCode(notification.getType());
        NotificationStatus status = NotificationStatus.fromCode(notification.getStatus());
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .typeName(type.getName())
                .title(notification.getTitle())
                .content(notification.getContent())
                .status(notification.getStatus())
                .statusName(status.getName())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .bizType(notification.getBizType())
                .bizId(notification.getBizId())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /**
     * 构建偏好响应
     */
    private PreferenceResponse buildPreferenceResponse(UserNotificationPreference preference) {
        return PreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .bookId(preference.getBookId())
                .emailEnabled(preference.getEmailEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .pushEnabled(preference.getPushEnabled())
                .subscribedTypes(preference.getSubscribedTypes())
                .build();
    }

    /**
     * 构建模板响应
     */
    private TemplateResponse buildTemplateResponse(NotificationTemplate template) {
        NotificationType type = NotificationType.fromCode(template.getType());
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .type(template.getType())
                .typeName(type.getName())
                .content(template.getContent())
                .titleTemplate(template.getTitleTemplate())
                .bizType(template.getBizType())
                .isEnabled(template.getIsEnabled())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
