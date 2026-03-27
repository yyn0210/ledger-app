package com.ledger.app.modules.transaction;

import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.dto.request.TransferRequest;
import com.ledger.app.modules.transaction.dto.request.UpdateTransactionRequest;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.ledger.app.modules.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 交易记录服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction testTransaction;
    private CreateTransactionRequest createRequest;
    private Account testAccount;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setBookId(100L);
        testTransaction.setUserId(100L);
        testTransaction.setType(2); // 支出
        testTransaction.setAmount(new BigDecimal("50.00"));
        testTransaction.setCategoryId(1L);
        testTransaction.setAccountId(1L);
        testTransaction.setTitle("测试交易");
        testTransaction.setTransactionDate(LocalDate.now());

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setBookId(100L);
        testAccount.setUserId(100L);
        testAccount.setName("测试账户");
        testAccount.setBalance(new BigDecimal("1000.00"));

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setBookId(100L);
        testCategory.setName("测试分类");

        createRequest = new CreateTransactionRequest();
        createRequest.setBookId(100L);
        createRequest.setUserId(100L);
        createRequest.setType(2);
        createRequest.setAmount(new BigDecimal("50.00"));
        createRequest.setCategoryId(1L);
        createRequest.setAccountId(1L);
        createRequest.setTitle("新交易");
        createRequest.setTransactionDate(LocalDate.now());
    }

    @Test
    void testCreateTransaction_Success() {
        // Arrange
        when(categoryRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testCategory);
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testAccount);
        when(transactionRepository.insert(any(Transaction.class))).thenReturn(1);
        when(accountRepository.decreaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);

        // Act
        Long result = transactionService.createTransaction(createRequest);

        // Assert
        assertNotNull(result);
        verify(transactionRepository).insert(any(Transaction.class));
        verify(accountRepository).decreaseBalance(1L, new BigDecimal("50.00"));
    }

    @Test
    void testCreateTransaction_CategoryNotFound() {
        // Arrange
        when(categoryRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(createRequest);
        });
        assertTrue(exception.getMessage().contains("分类不存在"));
    }

    @Test
    void testCreateTransaction_AccountNotFound() {
        // Arrange
        when(categoryRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testCategory);
        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction(createRequest);
        });
        assertTrue(exception.getMessage().contains("账户不存在"));
    }

    @Test
    void testGetTransaction_Success() {
        // Arrange
        when(transactionRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testTransaction);

        // Act
        TransactionResponse result = transactionService.getTransaction(1L, 100L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals("测试交易", result.getTitle());
        verify(transactionRepository).selectByIdAndBookId(1L, 100L);
    }

    @Test
    void testGetTransaction_NotFound() {
        // Arrange
        when(transactionRepository.selectByIdAndBookId(1L, 100L)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransaction(1L, 100L, 100L);
        });
        assertTrue(exception.getMessage().contains("交易记录不存在"));
    }

    @Test
    void testUpdateTransaction_Success() {
        // Arrange
        when(transactionRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testTransaction);
        when(transactionRepository.updateById(any(Transaction.class))).thenReturn(1);
        when(accountRepository.increaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);
        when(accountRepository.decreaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setTitle("更新后的交易");
        updateRequest.setAmount(new BigDecimal("80.00"));

        // Act
        transactionService.updateTransaction(1L, updateRequest, 100L, 100L);

        // Assert
        verify(transactionRepository).updateById(any(Transaction.class));
    }

    @Test
    void testDeleteTransaction_Success() {
        // Arrange
        when(transactionRepository.selectByIdAndBookId(1L, 100L)).thenReturn(testTransaction);
        when(transactionRepository.deleteById(1L)).thenReturn(1);
        when(accountRepository.increaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);

        // Act
        transactionService.deleteTransaction(1L, 100L, 100L);

        // Assert
        verify(transactionRepository).deleteById(1L);
        verify(accountRepository).increaseBalance(1L, new BigDecimal("50.00"));
    }

    @Test
    void testCreateTransfer_Success() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(new BigDecimal("5000.00"));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setBalance(new BigDecimal("1000.00"));

        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(fromAccount);
        when(accountRepository.selectByIdAndBookId(2L, 100L)).thenReturn(toAccount);
        when(transactionRepository.insert(any(Transaction.class))).thenReturn(1);
        when(transactionRepository.updateById(any(Transaction.class))).thenReturn(1);
        when(accountRepository.decreaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);
        when(accountRepository.increaseBalance(anyLong(), any(BigDecimal.class))).thenReturn(1);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBookId(100L);
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(new BigDecimal("1000.00"));
        transferRequest.setTransactionDate(LocalDate.now());

        // Act
        Long result = transactionService.createTransfer(transferRequest);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(2)).insert(any(Transaction.class));
        verify(accountRepository).decreaseBalance(1L, new BigDecimal("1000.00"));
        verify(accountRepository).increaseBalance(2L, new BigDecimal("1000.00"));
    }

    @Test
    void testCreateTransfer_InsufficientBalance() {
        // Arrange
        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setBalance(new BigDecimal("500.00"));

        when(accountRepository.selectByIdAndBookId(1L, 100L)).thenReturn(fromAccount);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBookId(100L);
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(new BigDecimal("1000.00"));
        transferRequest.setTransactionDate(LocalDate.now());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransfer(transferRequest);
        });
        assertTrue(exception.getMessage().contains("余额不足"));
    }
}
