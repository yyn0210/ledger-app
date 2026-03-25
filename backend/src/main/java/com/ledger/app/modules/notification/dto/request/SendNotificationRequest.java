package com.ledger.app.modules.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送通知请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 通知类型：1-邮件 2-短信 3-应用内 4-推送
     */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 模板编码（可选，使用模板时填写）
     */
    private String templateCode;

    /**
     * 模板变量（可选，配合模板使用）
     */
    private Map<String, Object> templateVars;

    /**
     * 业务类型（可选）
     */
    private String bizType;

    /**
     * 业务 ID（可选）
     */
    private Long bizId;
}
