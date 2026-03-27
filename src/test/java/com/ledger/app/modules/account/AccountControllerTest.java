package com.ledger.app.modules.account;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.account.controller.AccountController;
import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.service.AccountService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 账户控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private AccountResponse testAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();

        testAccount = new AccountResponse();
        testAccount.setId(1L);
        testAccount.setBookId(100L);
        testAccount.setName("测试账户");
        testAccount.setType(2);
        testAccount.setBalance(new BigDecimal("10000.00"));
        testAccount.setCurrency("CNY");
        testAccount.setIcon("🏦");
    }

    @Test
    void testGetAccounts() throws Exception {
        // Arrange
        List<AccountResponse> accounts = new ArrayList<>();
        accounts.add(testAccount);
        when(accountService.getAccounts(100L, 100L)).thenReturn(accounts);

        // Act & Assert
        mockMvc.perform(get("/api/accounts")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(accountService).getAccounts(100L, 100L);
    }

    @Test
    void testGetAccount() throws Exception {
        // Arrange
        when(accountService.getAccount(1L, 100L, 100L)).thenReturn(testAccount);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试账户"));

        verify(accountService).getAccount(1L, 100L, 100L);
    }

    @Test
    void testCreateAccount() throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setBookId(100L);
        request.setName("新账户");
        request.setType(1);
        request.setBalance(new BigDecimal("5000.00"));

        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":100,\"name\":\"新账户\",\"type\":1,\"balance\":5000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    void testUpdateAccount() throws Exception {
        // Arrange
        doNothing().when(accountService).updateAccount(anyLong(), any(UpdateAccountRequest.class), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/accounts/1")
                .param("bookId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新账户\",\"balance\":15000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(accountService).updateAccount(anyLong(), any(UpdateAccountRequest.class), anyLong(), anyLong());
    }

    @Test
    void testDeleteAccount() throws Exception {
        // Arrange
        doNothing().when(accountService).deleteAccount(anyLong(), anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/accounts/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(accountService).deleteAccount(1L, 100L, 100L);
    }

    @Test
    void testGetAccountSummary() throws Exception {
        // Arrange
        AccountSummaryResponse summary = new AccountSummaryResponse();
        summary.setTotalBalance(new BigDecimal("50000.00"));
        summary.setAccountCount(5);

        when(accountService.getAccountSummary(100L)).thenReturn(summary);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/summary")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalBalance").value("50000.00"))
                .andExpect(jsonPath("$.data.accountCount").value(5));

        verify(accountService).getAccountSummary(100L);
    }
}
