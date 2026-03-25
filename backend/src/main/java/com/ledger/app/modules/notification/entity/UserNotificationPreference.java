package com.ledger.app.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户通知偏好实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_notification_preference")
public class UserNotificationPreference {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 账本 ID（可选，null 表示全局设置）
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 是否启用邮件通知
     */
    @TableField("email_enabled")
    private Boolean emailEnabled;

    /**
     * 是否启用短信通知
     */
    @TableField("sms_enabled")
    private Boolean smsEnabled;

    /**
     * 是否启用应用内通知
     */
    @TableField("in_app_enabled")
    private Boolean inAppEnabled;

    /**
     * 是否启用推送通知
     */
    @TableField("push_enabled")
    private Boolean pushEnabled;

    /**
     * 订阅的通知类型（JSON 数组）
     */
    @TableField("subscribed_types")
    private String subscribedTypes;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
