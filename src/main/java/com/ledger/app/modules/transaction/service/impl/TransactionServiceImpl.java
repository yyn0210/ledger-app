package com.ledger.app.modules.transaction.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.transaction.dto.request.*;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.dto.response.TransactionPageResponse;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.ledger.app.modules.transaction.service.TransactionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public TransactionPageResponse getTransactions(
            Long bookId, Long userId,
            Integer page, Integer size,
            Integer type, Long categoryId, Long accountId,
            LocalDate startDate, LocalDate endDate,
            BigDecimal minAmount, BigDecimal maxAmount,
            String keyword
    ) {
        log.info("分页查询交易记录，bookId: {}, userId: {}, page: {}, size: {}", bookId, userId, page, size);

        Page<Transaction> mpPage = new Page<>(page != null ? page : 1, size != null ? size : 20);
        IPage<Transaction> result = transactionRepository.selectPageWithFilters(
                mpPage, bookId, type, categoryId, accountId, startDate, endDate, minAmount, maxAmount, keyword
        );

        List<TransactionResponse> responses = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new TransactionPageResponse(
                responses,
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                (int) result.getPages()
        );
    }

    @Override
    public TransactionResponse getTransaction(Long id, Long bookId, Long userId) {
        log.info("获取交易详情，id: {}, bookId: {}", id, bookId);

        Transaction transaction = transactionRepository.selectByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        return convertToResponse(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransaction(CreateTransactionRequest request) {
        log.info("创建交易记录，request: {}", request);

        // 验证交易类型
        TransactionType.fromCode(request.getType());

        // 验证分类是否存在
        Category category = categoryRepository.selectByIdAndBookId(request.getCategoryId(), request.getBookId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证账户是否存在
        Account account = accountRepository.selectByIdAndBookId(request.getAccountId(), request.getBookId());
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setBookId(request.getBookId());
        transaction.setUserId(request.getUserId());
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
        transaction.setTags(toJson(request.getTags()));
        transaction.setImageUrls(toJson(request.getImageUrls()));
        transaction.setIsTransfer(false);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.insert(transaction);

        // 更新账户余额
        updateAccountBalance(request.getAccountId(), request.getType(), request.getAmount(), true);

        log.info("交易记录创建成功，id: {}", transaction.getId());
        return transaction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTransaction(Long id, UpdateTransactionRequest request, Long bookId, Long userId) {
        log.info("更新交易记录，id: {}, bookId: {}", id, bookId);

        Transaction transaction = transactionRepository.selectByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 保存旧金额用于余额回滚
        BigDecimal oldAmount = transaction.getAmount();
        Integer oldType = transaction.getType();
        Long oldAccountId = transaction.getAccountId();

        // 更新字段
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
            transaction.setTags(toJson(request.getTags()));
        }
        if (request.getImageUrls() != null) {
            transaction.setImageUrls(toJson(request.getImageUrls()));
        }
        transaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.updateById(transaction);

        // 更新账户余额：先回滚旧记录，再应用新记录
        updateAccountBalance(oldAccountId, oldType, oldAmount, false); // 回滚旧
        updateAccountBalance(transaction.getAccountId(), transaction.getType(), transaction.getAmount(), true); // 应用新
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long id, Long bookId, Long userId) {
        log.info("删除交易记录，id: {}, bookId: {}", id, bookId);

        Transaction transaction = transactionRepository.selectByIdAndBookId(id, bookId);
        if (transaction == null) {
            throw new BusinessException("交易记录不存在");
        }

        // 回滚账户余额
        updateAccountBalance(transaction.getAccountId(), transaction.getType(), transaction.getAmount(), false);

        // 软删除
        transactionRepository.deleteById(id);

        log.info("交易记录删除成功，id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateTransactions(BatchCreateRequest request) {
        log.info("批量创建交易记录，count: {}", request.getTransactions().size());

        List<Long> ids = new ArrayList<>();
        for (CreateTransactionRequest txRequest : request.getTransactions()) {
            txRequest.setBookId(request.getBookId());
            txRequest.setUserId(request.getUserId());
            Long id = createTransaction(txRequest);
            ids.add(id);
        }

        log.info("批量创建成功，count: {}", ids.size());
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchDeleteTransactions(BatchDeleteRequest request) {
        log.info("批量删除交易记录，count: {}", request.getIds().size());

        int count = 0;
        for (Long id : request.getIds()) {
            try {
                deleteTransaction(id, request.getBookId(), null);
                count++;
            } catch (BusinessException e) {
                log.warn("删除交易记录失败，id: {}, error: {}", id, e.getMessage());
            }
        }

        log.info("批量删除成功，count: {}", count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransfer(TransferRequest request) {
        log.info("创建转账交易，fromAccountId: {}, toAccountId: {}, amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // 验证转出账户
        Account fromAccount = accountRepository.selectByIdAndBookId(request.getFromAccountId(), request.getBookId());
        if (fromAccount == null) {
            throw new BusinessException("转出账户不存在");
        }

        // 验证转入账户
        Account toAccount = accountRepository.selectByIdAndBookId(request.getToAccountId(), request.getBookId());
        if (toAccount == null) {
            throw new BusinessException("转入账户不存在");
        }

        // 验证余额是否充足
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BusinessException("转出账户余额不足");
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建支出记录（from 账户）
        Transaction expense = new Transaction();
        expense.setBookId(request.getBookId());
        expense.setUserId(request.getUserId());
        expense.setType(2); // 支出
        expense.setAmount(request.getAmount());
        expense.setCategoryId(1L); // 默认分类，实际使用时需要转账专用分类
        expense.setAccountId(request.getFromAccountId());
        expense.setToAccountId(request.getToAccountId());
        expense.setTitle(request.getTitle() != null ? request.getTitle() : "转账");
        expense.setDescription(request.getDescription());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setIsTransfer(true);
        expense.setCreatedAt(now);
        expense.setUpdatedAt(now);

        transactionRepository.insert(expense);

        // 创建收入记录（to 账户）
        Transaction income = new Transaction();
        income.setBookId(request.getBookId());
        income.setUserId(request.getUserId());
        income.setType(1); // 收入
        income.setAmount(request.getAmount());
        income.setCategoryId(1L);
        income.setAccountId(request.getToAccountId());
        income.setTitle(request.getTitle() != null ? request.getTitle() : "转账");
        income.setDescription(request.getDescription());
        income.setTransactionDate(request.getTransactionDate());
        income.setIsTransfer(true);
        income.setTransferToId(expense.getId());
        income.setCreatedAt(now);
        income.setUpdatedAt(now);

        transactionRepository.insert(income);

        // 关联两条记录
        expense.setTransferToId(income.getId());
        transactionRepository.updateById(expense);

        // 更新账户余额
        accountRepository.decreaseBalance(request.getFromAccountId(), request.getAmount());
        accountRepository.increaseBalance(request.getToAccountId(), request.getAmount());

        log.info("转账交易创建成功，expenseId: {}, incomeId: {}", expense.getId(), income.getId());
        return expense.getId();
    }

    /**
     * 更新账户余额
     *
     * @param accountId 账户 ID
     * @param type      交易类型
     * @param amount    金额
     * @param increase  true=增加余额，false=减少余额
     */
    private void updateAccountBalance(Long accountId, Integer type, BigDecimal amount, boolean increase) {
        if (type == 1) { // 收入
            if (increase) {
                accountRepository.increaseBalance(accountId, amount);
            } else {
                accountRepository.decreaseBalance(accountId, amount);
            }
        } else if (type == 2) { // 支出
            if (increase) {
                accountRepository.decreaseBalance(accountId, amount);
            } else {
                accountRepository.increaseBalance(accountId, amount);
            }
        }
        // 转账类型不直接影响余额（已在 createTransfer 中处理）
    }

    /**
     * 对象转 JSON 字符串
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失败", e);
            return null;
        }
    }

    /**
     * JSON 字符串转对象
     */
    private <T> List<T> fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            log.warn("JSON 反序列化失败", e);
            return null;
        }
    }

    /**
     * 转换 Entity 到 Response
     */
    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setBookId(transaction.getBookId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setCategoryId(transaction.getCategoryId());
        response.setAccountId(transaction.getAccountId());
        response.setToAccountId(transaction.getToAccountId());
        response.setTitle(transaction.getTitle());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setLocation(transaction.getLocation());
        response.setMerchant(transaction.getMerchant());
        response.setTags(fromJson(transaction.getTags(), String.class));
        response.setImageUrls(fromJson(transaction.getImageUrls(), String.class));
        response.setIsTransfer(transaction.getIsTransfer());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());

        // 设置类型名称
        try {
            response.setTypeName(TransactionType.fromCode(transaction.getType()).getName());
        } catch (IllegalArgumentException e) {
            response.setTypeName("未知");
        }

        // 设置分类名称
        if (transaction.getCategoryId() != null) {
            Category category = categoryRepository.selectById(transaction.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        // 设置账户名称
        if (transaction.getAccountId() != null) {
            Account account = accountRepository.selectById(transaction.getAccountId());
            if (account != null) {
                response.setAccountName(account.getName());
            }
        }

        // 设置目标账户名称
        if (transaction.getToAccountId() != null) {
            Account toAccount = accountRepository.selectById(transaction.getToAccountId());
            if (toAccount != null) {
                response.setToAccountName(toAccount.getName());
            }
        }

        return response;
    }
}
