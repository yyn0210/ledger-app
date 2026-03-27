package com.ledger.app.modules.account;

import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 账户服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account testAccount;
    private CreateAccountRequest createRequest;
    private UpdateAccountRequest updateRequest;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setBookId(100L);
        testAccount.setUserId(100L);
        testAccount.setName("测试账户");
        testAccount.setType(2);
        testAccount.setBalance(new BigDecimal("10000.00"));
        testAccount.setCurrency("CNY");
        testAccount.setIcon("🏦");
        testAccount.setColor("#E10602");
        testAccount.setIsInclude(true);
        testAccount.setSortOrder(1);

        createRequest = new CreateAccountRequest();
        createRequest.setBookId(100L);
        createRequest.setUserId(100L);
        createRequest.setName("新账户");
        createRequest.setType(1);
        createRequest.setBalance(new BigDecimal("5000.00"));
        createRequest.setCurrency("CNY");
        createRequest.setIsInclude(true);

        updateRequest = new UpdateAccountRequest();
        updateRequest.setName("更新后的账户");
        updateRequest.setBalance(new BigDecimal("15000.00"));
        updateRequest.setSortOrder(2);
    }

    @Test
    void testGetAccounts_Success() {
        // Arrange
        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);
        when(accountRepository.selectByBookIdAndUserId(100L, 100L)).thenReturn(accounts);

        // Act
        List<AccountResponse> result = accountService.getAccounts(100L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试账户", result.get(0).getName());
        verify(accountRepository).selectByBookIdAndUserId(100L, 100L);
    }

    @Test
    void testGetAccount_Success() {
        // Arrange
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);

        // Act
        AccountResponse result = accountService.getAccount(1L, 100L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals("测试账户", result.getName());
        verify(accountRepository).selectByIdAndBookId(1L, 100L);
    }

    @Test
    void testGetAccount_NotFound() {
        // Arrange
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccount(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("账户不存在"));
    }

    @Test
    void testGetAccount_NoPermission() {
        // Arrange
        testAccount.setUserId(200L); // Different user
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getAccount(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("无权访问"));
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        when(accountRepository.insert(any(Account.class))).thenReturn(1);

        // Act
        Long result = accountService.createAccount(createRequest);

        // Assert
        assertNotNull(result);
        verify(accountRepository).insert(any(Account.class));
    }

    @Test
    void testUpdateAccount_Success() {
        // Arrange
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);
        when(accountRepository.updateById(any(Account.class))).thenReturn(1);

        // Act
        accountService.updateAccount(1L, updateRequest, 100L, 100L);

        // Assert
        verify(accountRepository).selectByIdAndBookId(1L, 100L);
        verify(accountRepository).updateById(any(Account.class));
    }

    @Test
    void testUpdateAccount_NoPermission() {
        // Arrange
        testAccount.setUserId(200L);
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.updateAccount(1L, updateRequest, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("无权修改"));
    }

    @Test
    void testDeleteAccount_Success() {
        // Arrange
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);
        when(accountRepository.countTransactionsByAccountId(1L)).thenReturn(0L);
        when(accountRepository.deleteById(1L)).thenReturn(1);

        // Act
        accountService.deleteAccount(1L, 100L, 100L);

        // Assert
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void testDeleteAccount_HasTransactions() {
        // Arrange
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);
        when(accountRepository.countTransactionsByAccountId(1L)).thenReturn(5L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("已被交易记录使用"));
    }

    @Test
    void testDeleteAccount_NoPermission() {
        // Arrange
        testAccount.setUserId(200L);
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("无权删除"));
    }

    @Test
    void testGetAccountSummary_Success() {
        // Arrange
        when(accountRepository.sumByUserIdAndInclude(100L, true)).thenReturn(new BigDecimal("50000.00"));
        when(accountRepository.selectCount(any())).thenReturn(5L);

        // Act
        AccountSummaryResponse result = accountService.getAccountSummary(100L);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50000.00"), result.getTotalBalance());
        assertEquals(5, result.getAccountCount());
    }
}
