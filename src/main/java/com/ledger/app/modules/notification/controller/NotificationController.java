package com.ledger.app.modules.notification.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.notification.dto.request.SaveTemplateRequest;
import com.ledger.app.modules.notification.dto.request.SendNotificationRequest;
import com.ledger.app.modules.notification.dto.request.UpdatePreferenceRequest;
import com.ledger.app.modules.notification.dto.response.NotificationResponse;
import com.ledger.app.modules.notification.dto.response.PreferenceResponse;
import com.ledger.app.modules.notification.dto.response.TemplateResponse;
import com.ledger.app.modules.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知管理控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "通知管理", description = "通知发送、模板管理、偏好设置接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
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
     * 发送通知
     */
    @Operation(summary = "发送通知", description = "发送通知（邮件/短信/应用内/推送）")
    @PostMapping
    public Result<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        request.setUserId(userId);
        NotificationResponse response = notificationService.sendNotification(request);
        return Result.success(response);
    }

    /**
     * 获取通知列表
     */
    @Operation(summary = "获取通知列表", description = "获取用户的通知列表")
    @GetMapping
    public Result<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId, limit);
        return Result.success(notifications);
    }

    /**
     * 获取未读通知数量
     */
    @Operation(summary = "获取未读通知数量", description = "获取用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记通知为已读
     */
    @Operation(summary = "标记通知为已读", description = "标记指定通知为已读")
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     */
    @Operation(summary = "标记所有通知为已读", description = "标记用户所有通知为已读")
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }

    /**
     * 获取通知偏好
     */
    @Operation(summary = "获取通知偏好", description = "获取用户的通知偏好设置")
    @GetMapping("/preference")
    public Result<PreferenceResponse> getPreference(
            @RequestParam(required = false) Long bookId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        PreferenceResponse preference = notificationService.getPreference(userId, bookId);
        return Result.success(preference);
    }

    /**
     * 更新通知偏好
     */
    @Operation(summary = "更新通知偏好", description = "更新用户的通知偏好设置")
    @PutMapping("/preference")
    public Result<PreferenceResponse> updatePreference(
            @Valid @RequestBody UpdatePreferenceRequest request,
            @RequestParam(required = false) Long bookId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        PreferenceResponse preference = notificationService.updatePreference(userId, bookId, request);
        return Result.success(preference);
    }

    /**
     * 获取模板列表
     */
    @Operation(summary = "获取模板列表", description = "获取所有通知模板")
    @GetMapping("/templates")
    public Result<List<TemplateResponse>> getTemplates() {
        List<TemplateResponse> templates = notificationService.getTemplates();
        return Result.success(templates);
    }

    /**
     * 根据编码获取模板
     */
    @Operation(summary = "根据编码获取模板", description = "根据编码获取通知模板")
    @GetMapping("/templates/{code}")
    public Result<TemplateResponse> getTemplateByCode(@PathVariable String code) {
        TemplateResponse template = notificationService.getTemplateByCode(code);
        return Result.success(template);
    }

    /**
     * 保存模板
     */
    @Operation(summary = "保存模板", description = "创建或更新通知模板")
    @PostMapping("/templates")
    public Result<TemplateResponse> saveTemplate(
            @Valid @RequestBody SaveTemplateRequest request) {
        TemplateResponse template = notificationService.saveTemplate(request);
        return Result.success(template);
    }

    /**
     * 删除模板
     */
    @Operation(summary = "删除模板", description = "删除指定通知模板")
    @DeleteMapping("/templates/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return Result.success();
    }
}
