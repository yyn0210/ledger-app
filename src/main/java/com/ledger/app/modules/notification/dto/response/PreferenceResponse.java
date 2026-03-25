package com.ledger.app.modules.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户通知偏好响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreferenceResponse {

    /**
     * 偏好 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 账本 ID
     */
    private Long bookId;

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
     * 订阅的通知类型
     */
    private Object subscribedTypes;
}
