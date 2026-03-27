package com.ledger.app.modules.transaction;

import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.enums.TransactionType;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.ledger.app.modules.transaction.service.impl.TransactionServiceImpl;
import com.ledger.app.modules.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 交易服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private CreateTransactionRequest createRequest;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        createRequest = CreateTransactionRequest.builder()
                .bookId(1L)
                .categoryId(1L)
                .accountId(1L)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .note("测试交易")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .bookId(1L)
                .categoryId(1L)
                .accountId(1L)
                .type(TransactionType.EXPENSE.getCode())
                .amount(new BigDecimal("100.00"))
                .transactionDate(LocalDate.now())
                .note("测试交易")
                .build();
    }

    @Test
    void testCreateTransaction() {
        // 准备
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // 执行
        Long txId = transactionService.create(createRequest, 1L);

        // 验证
        assertNotNull(txId);
        assertEquals(1L, txId);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(accountService, times(1)).updateBalance(eq(1L), eq(new BigDecimal("-100.00")), eq(1L));
    }

    @Test
    void testCreateIncomeTransaction() {
        // 准备
        createRequest.setType(TransactionType.INCOME.getCode());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // 执行
        Long txId = transactionService.create(createRequest, 1L);

        // 验证
        assertNotNull(txId);
        verify(accountService, times(1)).updateBalance(eq(1L), eq(new BigDecimal("100.00")), eq(1L));
    }

    @Test
    void testGetTransactionById() {
        // 准备
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));

        // 执行
        Transaction result = transactionService.getById(1L, 1L);

        // 验证
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateTransaction() {
        // 准备
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));

        // 执行
        transaction.setNote("更新后的备注");
        transactionService.update(1L, createRequest, 1L);

        // 验证
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void testDeleteTransaction() {
        // 准备
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));

        // 执行
        transactionService.delete(1L, 1L);

        // 验证
        verify(transactionRepository, times(1)).deleteById(1L);
        verify(accountService, times(1)).updateBalance(eq(1L), eq(new BigDecimal("100.00")), eq(1L));
    }

    @Test
    void testGetTransactionsByBookId() {
        // 准备
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByBookId(eq(1L), any())).thenReturn(transactions);

        // 执行
        List<Transaction> result = transactionService.getTransactionsByBookId(1L, null, null, null, null, 1, 20);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
