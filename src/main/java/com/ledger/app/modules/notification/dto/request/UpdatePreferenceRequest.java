package com.ledger.app.modules.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 更新用户通知偏好请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferenceRequest {

    /**
     * 是否启用邮件通知
     */
    private Boolean emailEnabled;

    /**
     * 是否启用短信通知
     */
    private Boolean smsEnabled;

    /**
     * 是否启用应用内通知
     */
    private Boolean inAppEnabled;

    /**
     * 是否启用推送通知
     */
    private Boolean pushEnabled;

    /**
     * 订阅的通知类型（业务类型列表）
     */
    private Set<String> subscribedTypes;
}
