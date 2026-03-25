package com.ledger.app.modules.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建/更新通知模板请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveTemplateRequest {

    /**
     * 模板 ID（更新时填写）
     */
    private Long id;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    /**
     * 模板编码（唯一）
     */
    @NotBlank(message = "模板编码不能为空")
    private String code;

    /**
     * 通知类型：1-邮件 2-短信 3-应用内 4-推送
     */
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /**
     * 模板内容（支持占位符 {xxx}）
     */
    @NotBlank(message = "模板内容不能为空")
    private String content;

    /**
     * 模板标题（可选）
     */
    private String titleTemplate;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean isEnabled = true;
}
