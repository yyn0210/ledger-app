package com.ledger.app.modules.account;

import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.enums.AccountType;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.account.service.impl.AccountServiceImpl;
import com.ledger.app.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 账户服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .name("招商银行卡")
                .type(AccountType.DEBIT_CARD.getCode())
                .balance(new BigDecimal("10000.00"))
                .userId(1L)
                .build();
    }

    @Test
    void testCreateAccount() {
        // 准备
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // 执行
        Long accountId = accountService.create(account, 1L);

        // 验证
        assertNotNull(accountId);
        assertEquals(1L, accountId);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testGetAccountById() {
        // 准备
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // 执行
        Account result = accountService.getById(1L, 1L);

        // 验证
        assertNotNull(result);
        assertEquals("招商银行卡", result.getName());
        assertEquals(new BigDecimal("10000.00"), result.getBalance());
    }

    @Test
    void testGetAccountById_NotFound() {
        // 准备
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> accountService.getById(1L, 1L));
    }

    @Test
    void testGetAccountsByBookId() {
        // 准备
        List<Account> accounts = Arrays.asList(account);
        when(accountRepository.findByBookId(1L)).thenReturn(accounts);

        // 执行
        List<Account> result = accountService.getAccountsByBookId(1L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateAccount() {
        // 准备
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // 执行
        account.setName("更新后的银行卡");
        accountService.update(1L, account, 1L);

        // 验证
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testDeleteAccount() {
        // 准备
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // 执行
        accountService.delete(1L, 1L);

        // 验证
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateBalance() {
        // 准备
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // 执行
        accountService.updateBalance(1L, new BigDecimal("500.00"), 1L);

        // 验证
        verify(accountRepository, times(1)).save(account);
        assertEquals(new BigDecimal("10500.00"), account.getBalance());
    }
}
