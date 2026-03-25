package com.ledger.app.modules.account.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.dto.response.AccountSummaryResponse;
import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.enums.AccountType;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByBookId(Long bookId) {
        List<Account> accounts = accountRepository.findByBookId(bookId);
        return accounts.stream()
                .map(account -> {
                    AccountResponse response = AccountResponse.fromEntity(account);
                    // 设置类型名称
                    try {
                        AccountType type = AccountType.fromCode(account.getType());
                        response.setTypeName(type.getName());
                    } catch (IllegalArgumentException e) {
                        response.setTypeName("未知");
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByIdAndBookId(Long id, Long bookId) {
        Account account = accountRepository.findByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在或无权访问");
        }
        return AccountResponse.fromEntity(account);
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setBookId(request.getBookId());
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        account.setIcon(request.getIcon());
        account.setColor(request.getColor());
        account.setIsInclude(request.getIsInclude() != null ? request.getIsInclude() : 1);
        account.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        account.setDeleted(0);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.insert(account);
        log.info("创建账户成功：bookId={}, accountId={}, name={}", request.getBookId(), account.getId(), account.getName());

        return AccountResponse.fromEntity(account);
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(Long id, Long bookId, UpdateAccountRequest request) {
        Account account = accountRepository.findByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在或无权访问");
        }

        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getBalance() != null) {
            account.setBalance(request.getBalance());
        }
        if (request.getIcon() != null) {
            account.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            account.setColor(request.getColor());
        }
        if (request.getIsInclude() != null) {
            account.setIsInclude(request.getIsInclude());
        }
        if (request.getSortOrder() != null) {
            account.setSortOrder(request.getSortOrder());
        }

        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.updateById(account);
        log.info("更新账户成功：bookId={}, accountId={}", bookId, id);

        return AccountResponse.fromEntity(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id, Long bookId) {
        Account account = accountRepository.findByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在或无权访问");
        }

        // TODO: 检查是否有交易记录使用此账户（依赖交易模块）
        // Long count = transactionRepository.countByAccountId(id);
        // if (count > 0) {
        //     throw new BusinessException("该账户已被交易记录使用，无法删除");
        // }

        // 软删除
        account.setDeleted(1);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.updateById(account);
        log.info("删除账户成功：bookId={}, accountId={}", bookId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(Long bookId, Long userId) {
        // 获取账户数量
        int accountCount = accountRepository.countByBookId(bookId);

        // 统计总资产（只计算 is_include=1 的账户）
        BigDecimal totalBalance = accountRepository.sumTotalAssets(userId);

        // 按类型统计
        List<AccountRepository.TypeBalance> typeBalances = accountRepository.sumByType(userId);
        List<AccountSummaryResponse.TypeBalance> byType = typeBalances.stream()
                .map(tb -> {
                    try {
                        AccountType type = AccountType.fromCode(tb.getType());
                        return AccountSummaryResponse.TypeBalance.builder()
                                .type(tb.getType())
                                .typeName(type.getName())
                                .balance(tb.getBalance())
                                .build();
                    } catch (IllegalArgumentException e) {
                        return AccountSummaryResponse.TypeBalance.builder()
                                .type(tb.getType())
                                .typeName("未知")
                                .balance(tb.getBalance())
                                .build();
                    }
                })
                .collect(Collectors.toList());

        // TODO: 统计总收入和总支出（依赖交易模块）
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        return AccountSummaryResponse.builder()
                .totalBalance(totalBalance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .accountCount(accountCount)
                .byType(byType)
                .build();
    }
}
