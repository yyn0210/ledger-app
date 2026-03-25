package com.ledger.app.modules.websocket.handler;

import com.ledger.app.modules.websocket.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接处理器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Component
public class WebSocketConnectionHandler extends TextWebSocketHandler {

    /**
     * 在线用户会话管理
     */
    private static final ConcurrentHashMap<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineSessions.put(userId, session);
            log.info("WebSocket 连接建立：userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.debug("收到 WebSocket 消息：userId={}, payload={}", userId, message.getPayload());
        
        // 可以处理客户端发送的消息
        super.handleTextMessage(session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            onlineSessions.remove(userId);
            log.info("WebSocket 连接关闭：userId={}, sessionId={}, status={}", userId, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket 传输错误：userId={}, error={}", userId, exception.getMessage());
    }

    /**
     * 获取会话中的用户 ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            java.security.Principal principal = session.getPrincipal();
            if (principal instanceof com.ledger.app.modules.websocket.interceptor.WebSocketAuthInterceptor.WebSocketUser) {
                return ((com.ledger.app.modules.websocket.interceptor.WebSocketAuthInterceptor.WebSocketUser) principal).getUserId();
            }
            // 从 attributes 获取
            String userIdStr = (String) session.getAttributes().get("userId");
            if (userIdStr != null) {
                return Long.parseLong(userIdStr);
            }
        } catch (Exception e) {
            log.warn("获取用户 ID 失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return onlineSessions.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return onlineSessions.containsKey(userId);
    }
}
