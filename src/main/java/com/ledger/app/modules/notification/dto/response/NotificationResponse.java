package com.ledger.app.modules.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {

    /**
     * 通知 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 通知类型：1-邮件 2-短信 3-应用内 4-推送
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 状态：1-待发送 2-已发送 3-已送达 4-已读 5-发送失败
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务 ID
     */
    private Long bizId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
