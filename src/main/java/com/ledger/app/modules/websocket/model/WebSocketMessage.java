package com.ledger.app.modules.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket 消息模型
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消息内容
     */
    private T data;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 发送者 ID
     */
    private Long senderId;

    /**
     * 接收者 ID（可选，用于点对点消息）
     */
    private Long receiverId;

    /**
     * 账本 ID（可选，用于过滤）
     */
    private Long bookId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容描述
     */
    private String description;
}
