package com.ledger.app.modules.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知模板响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResponse {

    /**
     * 模板 ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板编码
     */
    private String code;

    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板标题
     */
    private String titleTemplate;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
