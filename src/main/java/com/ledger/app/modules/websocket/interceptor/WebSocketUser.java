package com.ledger.app.modules.websocket.interceptor;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket 用户实现（内部类）
 */
class WebSocketUser implements java.security.Principal {
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
