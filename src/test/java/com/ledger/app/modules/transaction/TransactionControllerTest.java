package com.ledger.app.modules.transaction;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.transaction.controller.TransactionController;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.dto.request.TransferRequest;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 交易记录控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private TransactionResponse testTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();

        testTransaction = new TransactionResponse();
        testTransaction.setId(1L);
        testTransaction.setBookId(100L);
        testTransaction.setTitle("测试交易");
        testTransaction.setType(2);
        testTransaction.setAmount(new BigDecimal("50.00"));
    }

    @Test
    void testGetTransactions() throws Exception {
        // Arrange
        when(transactionService.getTransactions(anyLong(), anyLong(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .param("bookId", "100")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk());

        verify(transactionService).getTransactions(anyLong(), anyLong(), anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetTransaction() throws Exception {
        // Arrange
        when(transactionService.getTransaction(1L, 100L, 100L)).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试交易"));

        verify(transactionService).getTransaction(1L, 100L, 100L);
    }

    @Test
    void testCreateTransaction() throws Exception {
        // Arrange
        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":100,\"type\":2,\"amount\":50.00,\"categoryId\":1,\"accountId\":1,\"transactionDate\":\"2026-03-24\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(transactionService).createTransaction(any(CreateTransactionRequest.class));
    }

    @Test
    void testUpdateTransaction() throws Exception {
        // Arrange
        doNothing().when(transactionService).updateTransaction(anyLong(), any(), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/transactions/1")
                .param("bookId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"更新交易\",\"amount\":80.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(transactionService).updateTransaction(anyLong(), any(), anyLong(), anyLong());
    }

    @Test
    void testDeleteTransaction() throws Exception {
        // Arrange
        doNothing().when(transactionService).deleteTransaction(anyLong(), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/transactions/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(transactionService).deleteTransaction(1L, 100L, 100L);
    }

    @Test
    void testCreateTransfer() throws Exception {
        // Arrange
        when(transactionService.createTransfer(any(TransferRequest.class))).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":100,\"fromAccountId\":1,\"toAccountId\":2,\"amount\":1000.00,\"transactionDate\":\"2026-03-24\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(transactionService).createTransfer(any(TransferRequest.class));
    }
}
