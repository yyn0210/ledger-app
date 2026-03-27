package com.ledger.app.modules.transaction.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.ledger.app.modules.transaction.service.TransactionService;
import com.ledger.app.modules.websocket.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final WebSocketMessageService webSocketMessageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public TransactionPageResponse getTransactions(Long bookId, Integer page, Integer size,
                                                    Integer type, Long categoryId, Long accountId,
                                                    String startDate, String endDate,
                                                    String minAmount, String maxAmount, String keyword) {
        // 构建分页对象
        Page<Transaction> pageObj = new Page<>(page != null ? page : 1, size != null ? Math.min(size, 100) : 20);

        // 解析参数
        Integer typeValue = type;
        LocalDate startDateValue = StringUtils.hasText(startDate) ? LocalDate.parse(startDate, DATE_FORMATTER) : null;
        LocalDate endDateValue = StringUtils.hasText(endDate) ? LocalDate.parse(endDate, DATE_FORMATTER) : null;
        BigDecimal minAmountValue = StringUtils.hasText(minAmount) ? new BigDecimal(minAmount) : null;
        BigDecimal maxAmountValue = StringUtils.hasText(maxAmount) ? new BigDecimal(maxAmount) : null;

        // 分页查询
        IPage<Transaction> result = transactionRepository.selectPageWithFilter(
                pageObj, bookId, typeValue, categoryId, accountId,
                startDateValue, endDateValue, minAmountValue, maxAmountValue, keyword);

        // 转换为响应
        List<TransactionResponse> list = result.getRecords().stream()
                .map(this::buildTransactionResponse)
                .toList();

        return TransactionPageResponse.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByIdAndBookId(Long id, Long bookId) {
        Transaction transaction = transactionRepository.findByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在或无权访问");
        }
        return buildTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse createTransaction(Long userId, CreateTransactionRequest request) {
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setBookId(request.getBookId());
        transaction.setUserId(userId);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setAccountId(request.getAccountId());
        transaction.setToAccountId(request.getToAccountId());
        transaction.setTitle(request.getTitle());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setLocation(request.getLocation());
        transaction.setMerchant(request.getMerchant());
        transaction.setTags(parseListToJson(request.getTags()));
        transaction.setImageUrls(parseListToJson(request.getImageUrls()));
        transaction.setIsTransfer(request.getType() != null && request.getType() == 3 ? 1 : 0);
        transaction.setDeleted(0);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.insert(transaction);

        // 更新账户余额
        updateAccountBalance(transaction);

        // 发送 WebSocket 通知
        sendTransactionCreatedNotification(userId, transaction);

        log.info("创建交易记录成功：userId={}, transactionId={}, amount={}", userId, transaction.getId(), transaction.getAmount());

        return buildTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long id, Long bookId, Long userId, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在或无权访问");
        }

        // 保存旧金额用于余额回滚
        BigDecimal oldAmount = transaction.getAmount();
        Integer oldType = transaction.getType();
        Long oldAccountId = transaction.getAccountId();

        // 更新字段
        if (request.getBookId() != null) {
            transaction.setBookId(request.getBookId());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategoryId() != null) {
            transaction.setCategoryId(request.getCategoryId());
        }
        if (request.getAccountId() != null) {
            transaction.setAccountId(request.getAccountId());
        }
        if (request.getTitle() != null) {
            transaction.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getTransactionDate() != null) {
            transaction.setTransactionDate(request.getTransactionDate());
        }
        if (request.getLocation() != null) {
            transaction.setLocation(request.getLocation());
        }
        if (request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
        }
        if (request.getTags() != null) {
            transaction.setTags(parseListToJson(request.getTags()));
        }
        if (request.getImageUrls() != null) {
            transaction.setImageUrls(parseListToJson(request.getImageUrls()));
        }

        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.updateById(transaction);

        // 更新账户余额（先回滚旧的，再应用新的）
        rollbackAccountBalance(oldAccountId, oldAmount, oldType);
        updateAccountBalance(transaction);

        // 发送 WebSocket 通知
        sendTransactionUpdatedNotification(userId, transaction);

        log.info("更新交易记录成功：userId={}, transactionId={}", userId, id);

        return buildTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id, Long bookId) {
        Transaction transaction = transactionRepository.findByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在或无权访问");
        }

        // 回滚账户余额
        rollbackAccountBalance(transaction.getAccountId(), transaction.getAmount(), transaction.getType());

        // 软删除
        transaction.setDeleted(1);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.updateById(transaction);

        // 发送 WebSocket 通知
        sendTransactionDeletedNotification(transaction.getUserId(), transaction.getBookId(), id);

        log.info("删除交易记录成功：transactionId={}", id);
    }

    @Override
    @Transactional
    public BatchCreateResponse batchCreate(Long userId, BatchCreateRequest request) {
        List<Long> ids = new ArrayList<>();
        for (CreateTransactionRequest txRequest : request.getTransactions()) {
            txRequest.setBookId(request.getBookId());
            TransactionResponse response = createTransaction(userId, txRequest);
            ids.add(response.getId());
        }
        return new BatchCreateResponse(ids.size(), ids);
    }

    @Override
    @Transactional
    public BatchDeleteResponse batchDelete(Long bookId, BatchDeleteRequest request) {
        int deletedCount = 0;
        for (Long id : request.getIds()) {
            try {
                deleteTransaction(id, bookId);
                deletedCount++;
            } catch (Exception e) {
                log.warn("删除交易记录失败：id={}, error={}", id, e.getMessage());
            }
        }
        return new BatchDeleteResponse(deletedCount);
    }

    @Override
    @Transactional
    public TransactionResponse createTransfer(Long userId, TransferRequest request) {
        // 验证账户
        var fromAccount = accountRepository.selectById(request.getFromAccountId());
        var toAccount = accountRepository.selectById(request.getToAccountId());
        if (fromAccount == null || toAccount == null) {
            throw new BusinessException("账户不存在");
        }

        // 创建支出记录（from 账户）
        Transaction expense = new Transaction();
        expense.setBookId(request.getBookId());
        expense.setUserId(userId);
        expense.setType(2); // 支出
        expense.setAmount(request.getAmount());
        expense.setAccountId(request.getFromAccountId());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setIsTransfer(1);
        expense.setDeleted(0);
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        // 创建收入记录（to 账户）
        Transaction income = new Transaction();
        income.setBookId(request.getBookId());
        income.setUserId(userId);
        income.setType(1); // 收入
        income.setAmount(request.getAmount());
        income.setAccountId(request.getToAccountId());
        income.setTitle(request.getTitle());
        income.setDescription(request.getDescription());
        income.setTransactionDate(request.getTransactionDate());
        income.setIsTransfer(1);
        income.setDeleted(0);
        income.setCreatedAt(LocalDateTime.now());
        income.setUpdatedAt(LocalDateTime.now());

        // 插入两条记录
        transactionRepository.insert(expense);
        transactionRepository.insert(income);

        // 关联两条记录
        expense.setTransferToId(income.getId());
        income.setTransferToId(expense.getId());
        transactionRepository.updateById(expense);
        transactionRepository.updateById(income);

        // 更新账户余额
        accountRepository.decreaseBalance(request.getFromAccountId(), request.getAmount());
        accountRepository.increaseBalance(request.getToAccountId(), request.getAmount());

        log.info("创建转账交易成功：userId={}, fromAccountId={}, toAccountId={}, amount={}",
                userId, request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        return buildTransactionResponse(expense);
    }

    /**
     * 更新账户余额
     */
    private void updateAccountBalance(Transaction transaction) {
        if (transaction.getType() == 1) { // 收入
            accountRepository.increaseBalance(transaction.getAccountId(), transaction.getAmount());
        } else if (transaction.getType() == 2) { // 支出
            accountRepository.decreaseBalance(transaction.getAccountId(), transaction.getAmount());
        }
        // 转账不更新总资产
    }

    /**
     * 回滚账户余额
     */
    private void rollbackAccountBalance(Long accountId, BigDecimal amount, Integer type) {
        if (type == 1) { // 收入回滚：减少
            accountRepository.decreaseBalance(accountId, amount);
        } else if (type == 2) { // 支出回滚：增加
            accountRepository.increaseBalance(accountId, amount);
        }
    }

    /**
     * 构建交易响应（包含关联名称）
     */
    private TransactionResponse buildTransactionResponse(Transaction transaction) {
        TransactionResponse response = TransactionResponse.fromEntity(transaction);
        
        // 设置类型名称
        try {
            TransactionType type = TransactionType.fromCode(transaction.getType());
            response.setTypeName(type.getName());
        } catch (IllegalArgumentException e) {
            response.setTypeName("未知");
        }

        return response;
    }

    /**
     * 列表转 JSON
     */
    private String parseListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 发送交易创建通知
     */
    private void sendTransactionCreatedNotification(Long userId, Transaction transaction) {
        try {
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("id", transaction.getId());
            transactionData.put("bookId", transaction.getBookId());
            transactionData.put("type", transaction.getType());
            transactionData.put("amount", transaction.getAmount());
            transactionData.put("categoryId", transaction.getCategoryId());
            transactionData.put("accountId", transaction.getAccountId());
            transactionData.put("title", transaction.getTitle());
            transactionData.put("transactionDate", transaction.getTransactionDate());

            webSocketMessageService.sendTransactionCreated(userId, transaction.getBookId(), transactionData);
        } catch (Exception e) {
            log.error("发送交易创建 WebSocket 通知失败：error={}", e.getMessage());
        }
    }

    /**
     * 发送交易更新通知
     */
    private void sendTransactionUpdatedNotification(Long userId, Transaction transaction) {
        try {
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("id", transaction.getId());
            transactionData.put("bookId", transaction.getBookId());
            transactionData.put("type", transaction.getType());
            transactionData.put("amount", transaction.getAmount());
            transactionData.put("categoryId", transaction.getCategoryId());
            transactionData.put("accountId", transaction.getAccountId());
            transactionData.put("title", transaction.getTitle());
            transactionData.put("transactionDate", transaction.getTransactionDate());

            webSocketMessageService.sendTransactionUpdated(userId, transaction.getBookId(), transactionData);
        } catch (Exception e) {
            log.error("发送交易更新 WebSocket 通知失败：error={}", e.getMessage());
        }
    }

    /**
     * 发送交易删除通知
     */
    private void sendTransactionDeletedNotification(Long userId, Long bookId, Long transactionId) {
        try {
            webSocketMessageService.sendTransactionDeleted(userId, bookId, transactionId);
        } catch (Exception e) {
            log.error("发送交易删除 WebSocket 通知失败：error={}", e.getMessage());
        }
    }
}
