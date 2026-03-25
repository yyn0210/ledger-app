package com.ledger.app.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知记录实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification {

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
     * 通知类型：1-邮件 2-短信 3-应用内 4-推送
     */
    @TableField("type")
    private Integer type;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 模板 ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 状态：1-待发送 2-已发送 3-已送达 4-已读 5-发送失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 发送时间
     */
    @TableField("sent_at")
    private LocalDateTime sentAt;

    /**
     * 阅读时间
     */
    @TableField("read_at")
    private LocalDateTime readAt;

    /**
     * 业务类型（transaction/budget/savings 等）
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 业务 ID
     */
    @TableField("biz_id")
    private Long bizId;

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
