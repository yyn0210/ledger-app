package com.ledger.app.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知模板实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification_template")
public class NotificationTemplate {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("name")
    private String name;

    /**
     * 模板编码（唯一）
     */
    @TableField("code")
    private String code;

    /**
     * 通知类型：1-邮件 2-短信 3-应用内 4-推送
     */
    @TableField("type")
    private Integer type;

    /**
     * 模板内容（支持占位符 {xxx}）
     */
    @TableField("content")
    private String content;

    /**
     * 模板标题（邮件/推送用）
     */
    @TableField("title_template")
    private String titleTemplate;

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 模板变量定义（JSON）
     */
    @TableField("variables")
    private String variables;

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
