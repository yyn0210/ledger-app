package com.ledger.app.modules.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.websocket.model.MessageType;
import com.ledger.app.modules.websocket.model.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WebSocket 消息推送服务
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 发送交易创建通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param transactionData 交易数据
     */
    @Async
    public void sendTransactionCreated(Long userId, Long bookId, Object transactionData) {
        WebSocketMessage<Object> message = WebSocketMessage.builder()
                .type(MessageType.TRANSACTION_CREATED.getCode())
                .data(transactionData)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("交易创建")
                .description("新增一笔交易记录")
                .build();

        sendToUser(userId, "/topic/transactions", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/transactions", message);
    }

    /**
     * 发送交易更新通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param transactionData 交易数据
     */
    @Async
    public void sendTransactionUpdated(Long userId, Long bookId, Object transactionData) {
        WebSocketMessage<Object> message = WebSocketMessage.builder()
                .type(MessageType.TRANSACTION_UPDATED.getCode())
                .data(transactionData)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("交易更新")
                .description("交易记录已更新")
                .build();

        sendToUser(userId, "/topic/transactions", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/transactions", message);
    }

    /**
     * 发送交易删除通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param transactionId 交易 ID
     */
    @Async
    public void sendTransactionDeleted(Long userId, Long bookId, Long transactionId) {
        WebSocketMessage<Long> message = WebSocketMessage.<Long>builder()
                .type(MessageType.TRANSACTION_DELETED.getCode())
                .data(transactionId)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("交易删除")
                .description("交易记录已删除")
                .build();

        sendToUser(userId, "/topic/transactions", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/transactions", message);
    }

    /**
     * 发送账户余额更新通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param accountId 账户 ID
     * @param newBalance 新余额
     * @param changeAmount 变更金额
     */
    @Async
    public void sendBalanceChanged(Long userId, Long bookId, Long accountId, java.math.BigDecimal newBalance, java.math.BigDecimal changeAmount) {
        BalanceChangeData data = new BalanceChangeData(accountId, newBalance, changeAmount);
        
        WebSocketMessage<BalanceChangeData> message = WebSocketMessage.<BalanceChangeData>builder()
                .type(MessageType.BALANCE_CHANGED.getCode())
                .data(data)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("余额变更")
                .description("账户余额已更新")
                .build();

        sendToUser(userId, "/topic/accounts", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/accounts", message);
    }

    /**
     * 发送预算预警通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param budgetId 预算 ID
     * @param budgetName 预算名称
     * @param progress 进度百分比
     */
    @Async
    public void sendBudgetAlert(Long userId, Long bookId, Long budgetId, String budgetName, java.math.BigDecimal progress) {
        BudgetAlertData data = new BudgetAlertData(budgetId, budgetName, progress);
        
        WebSocketMessage<BudgetAlertData> message = WebSocketMessage.<BudgetAlertData>builder()
                .type(MessageType.BUDGET_ALERT.getCode())
                .data(data)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("预算预警")
                .description("预算\"" + budgetName + "\"已达到预警线")
                .build();

        sendToUser(userId, "/topic/budgets", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/budgets", message);
    }

    /**
     * 发送预算超支通知
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID
     * @param budgetId 预算 ID
     * @param budgetName 预算名称
     * @param progress 进度百分比
     */
    @Async
    public void sendBudgetOverdue(Long userId, Long bookId, Long budgetId, String budgetName, java.math.BigDecimal progress) {
        BudgetAlertData data = new BudgetAlertData(budgetId, budgetName, progress);
        
        WebSocketMessage<BudgetAlertData> message = WebSocketMessage.<BudgetAlertData>builder()
                .type(MessageType.BUDGET_OVERDUE.getCode())
                .data(data)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(userId)
                .bookId(bookId)
                .title("预算超支")
                .description("预算\"" + budgetName + "\"已超支")
                .build();

        sendToUser(userId, "/topic/budgets", message);
        sendToBook(bookId, "/topic/book/" + bookId + "/budgets", message);
    }

    /**
     * 发送系统通知
     *
     * @param userId 用户 ID
     * @param title 标题
     * @param description 描述
     */
    @Async
    public void sendSystemNotification(Long userId, String title, String description) {
        WebSocketMessage<Object> message = WebSocketMessage.builder()
                .type(MessageType.SYSTEM_NOTIFICATION.getCode())
                .data(null)
                .timestamp(LocalDateTime.now())
                .messageId(UUID.randomUUID().toString())
                .senderId(0L)
                .title(title)
                .description(description)
                .build();

        sendToUser(userId, "/topic/notifications", message);
    }

    /**
     * 发送到用户
     */
    private void sendToUser(Long userId, String destination, WebSocketMessage<?> message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    destination,
                    message
            );
            log.debug("发送用户消息：userId={}, destination={}, type={}", userId, destination, message.getType());
        } catch (Exception e) {
            log.error("发送用户消息失败：userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 发送到账本主题
     */
    private void sendToBook(Long bookId, String destination, WebSocketMessage<?> message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.debug("发送账本消息：bookId={}, destination={}, type={}", bookId, destination, message.getType());
        } catch (Exception e) {
            log.error("发送账本消息失败：bookId={}, error={}", bookId, e.getMessage());
        }
    }

    /**
     * 余额变更数据
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BalanceChangeData {
        private Long accountId;
        private java.math.BigDecimal newBalance;
        private java.math.BigDecimal changeAmount;
    }

    /**
     * 预算预警数据
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BudgetAlertData {
        private Long budgetId;
        private String budgetName;
        private java.math.BigDecimal progress;
    }
}
