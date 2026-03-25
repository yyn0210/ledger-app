package com.ledger.app.modules.websocket.interceptor;

import com.ledger.app.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
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
                System.out.println("WebSocket 连接被拒绝：缺少有效的认证 token");
                return null; // 拒绝连接
            }

            String token = authorization.substring(7);
            try {
                Long userId = authService.getUserIdFromToken(token);
                if (userId != null) {
                    accessor.setUser(new WebSocketUser(userId));
                    System.out.println("WebSocket 用户认证成功：userId=" + userId);
                } else {
                    System.out.println("WebSocket 连接被拒绝：无效的 token");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("WebSocket 连接被拒绝：token 验证失败 - " + e.getMessage());
                return null;
            }
        }

        return message;
    }

    /**
     * WebSocket 用户实现
     */
    public static class WebSocketUser implements java.security.Principal {
        private final Long userId;

        public WebSocketUser(Long userId) {
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
