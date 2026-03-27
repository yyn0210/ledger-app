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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountResponse> getAccounts(Long bookId, Long userId) {
        log.info("获取账户列表，bookId: {}, userId: {}", bookId, userId);

        List<Account> accounts = accountRepository.selectByBookIdAndUserId(bookId, userId);
        return accounts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccount(Long id, Long bookId, Long userId) {
        log.info("获取账户详情，id: {}, bookId: {}, userId: {}", id, bookId, userId);

        Account account = accountRepository.selectByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 权限检查：确保账户属于当前用户
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此账户");
        }

        return convertToResponse(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAccount(CreateAccountRequest request) {
        log.info("创建账户，request: {}", request);

        // 验证账户类型
        AccountType.fromCode(request.getType());

        // 创建账户
        Account account = new Account();
        account.setBookId(request.getBookId());
        account.setUserId(request.getUserId());
        account.setName(request.getName());
        account.setType(request.getType());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CNY");
        account.setIcon(request.getIcon());
        account.setColor(request.getColor());
        account.setIsInclude(request.getIsInclude() != null ? request.getIsInclude() : true);
        account.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        accountRepository.insert(account);

        log.info("账户创建成功，id: {}", account.getId());
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccount(Long id, UpdateAccountRequest request, Long bookId, Long userId) {
        log.info("更新账户，id: {}, bookId: {}, userId: {}", id, bookId, userId);

        Account account = accountRepository.selectByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 权限检查
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此账户");
        }

        // 更新字段
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

        accountRepository.updateById(account);

        log.info("账户更新成功，id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long id, Long bookId, Long userId) {
        log.info("删除账户，id: {}, bookId: {}, userId: {}", id, bookId, userId);

        Account account = accountRepository.selectByIdAndBookId(id, bookId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 权限检查
        if (!account.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此账户");
        }

        // 检查是否被交易记录引用
        Long count = accountRepository.countTransactionsByAccountId(id);
        if (count > 0) {
            throw new BusinessException("该账户已被交易记录使用，无法删除");
        }

        // 软删除
        accountRepository.deleteById(id);

        log.info("账户删除成功，id: {}", id);
    }

    @Override
    public AccountSummaryResponse getAccountSummary(Long userId) {
        log.info("获取账户汇总统计，userId: {}", userId);

        AccountSummaryResponse response = new AccountSummaryResponse();

        // 统计总余额（只计算 is_include=true 的账户）
        BigDecimal totalBalance = accountRepository.sumByUserIdAndInclude(userId, true);
        response.setTotalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO);

        // 统计账户数量
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .eq(Account::getDeleted, 0);
        Long accountCount = accountRepository.selectCount(wrapper);
        response.setAccountCount(accountCount.intValue());

        // 按类型分组统计
        List<AccountRepository.AccountTypeBalance> typeBalances = accountRepository.sumByType(userId);
        List<AccountSummaryResponse.AccountTypeBalance> byType = new ArrayList<>();

        for (AccountRepository.AccountTypeBalance balance : typeBalances) {
            AccountSummaryResponse.AccountTypeBalance typeBalance = new AccountSummaryResponse.AccountTypeBalance();
            typeBalance.setType(balance.getType());
            typeBalance.setBalance(balance.getBalance() != null ? balance.getBalance() : BigDecimal.ZERO);

            // 获取类型名称和图标
            try {
                AccountType accountType = AccountType.fromCode(balance.getType());
                typeBalance.setTypeName(accountType.getName());
                typeBalance.setTypeIcon(accountType.getIcon());
            } catch (IllegalArgumentException e) {
                typeBalance.setTypeName("未知");
                typeBalance.setTypeIcon("❓");
            }

            byType.add(typeBalance);
        }

        response.setByType(byType);

        log.info("账户汇总统计完成，totalBalance: {}, accountCount: {}", response.getTotalBalance(), response.getAccountCount());
        return response;
    }

    /**
     * 转换 Entity 到 Response
     */
    private AccountResponse convertToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setBookId(account.getBookId());
        response.setName(account.getName());
        response.setType(account.getType());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setIcon(account.getIcon());
        response.setColor(account.getColor());
        response.setIsInclude(account.getIsInclude());
        response.setSortOrder(account.getSortOrder());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());

        // 设置类型名称和图标
        try {
            AccountType accountType = AccountType.fromCode(account.getType());
            response.setTypeName(accountType.getName());
            response.setTypeIcon(accountType.getIcon());
        } catch (IllegalArgumentException e) {
            response.setTypeName("未知");
            response.setTypeIcon("❓");
        }

        return response;
    }
}
