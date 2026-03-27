package com.ledger.app.modules.websocket.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.websocket.handler.WebSocketConnectionHandler;
import com.ledger.app.modules.websocket.service.WebSocketMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket 管理控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "WebSocket 管理", description = "WebSocket 连接管理和测试接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketController {

    private final WebSocketMessageService messageService;
    private final WebSocketConnectionHandler connectionHandler;

    /**
     * 获取在线用户数量
     */
    @Operation(summary = "获取在线用户数量", description = "获取当前 WebSocket 在线用户数量")
    @GetMapping("/online-count")
    public Result<Integer> getOnlineCount() {
        return Result.success(connectionHandler.getOnlineCount());
    }

    /**
     * 检查用户是否在线
     */
    @Operation(summary = "检查用户是否在线", description = "检查指定用户是否 WebSocket 在线")
    @GetMapping("/user/{userId}/online")
    public Result<Boolean> isUserOnline(@PathVariable Long userId) {
        return Result.success(connectionHandler.isUserOnline(userId));
    }

    /**
     * 发送测试消息
     */
    @Operation(summary = "发送测试消息", description = "向指定用户发送测试消息")
    @PostMapping("/test/send")
    public Result<Void> sendTestMessage(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "测试消息") String message) {
        messageService.sendSystemNotification(userId, "WebSocket 测试", message);
        return Result.success();
    }
}
