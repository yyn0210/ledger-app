package com.ledger.app.modules.websocket.interceptor;

import com.ledger.app.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * WebSocket 认证拦截器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final AuthService authService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorization = accessor.getFirstNativeHeader("Authorization");
            
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                log.warn("WebSocket 连接被拒绝：缺少有效的认证 token");
                return null; // 拒绝连接
            }

            String token = authorization.substring(7);
            try {
                Long userId = authService.getUserIdFromToken(token);
                if (userId != null) {
                    accessor.setUser(new WebSocketUser(userId));
                    log.info("WebSocket 用户认证成功：userId={}", userId);
                } else {
                    log.warn("WebSocket 连接被拒绝：无效的 token");
                    return null;
                }
            } catch (Exception e) {
                log.warn("WebSocket 连接被拒绝：token 验证失败 - {}", e.getMessage());
                return null;
            }
        }

        return message;
    }

    /**
     * WebSocket 用户实现
     */
    private static class WebSocketUser implements java.security.Principal {
        private final Long userId;

        WebSocketUser(Long userId) {
            this.userId = userId;
        }

        @Override
        public String getName() {
            return userId.toString();
        }

        public Long getUserId() {
            return userId;
        }
    }
}
