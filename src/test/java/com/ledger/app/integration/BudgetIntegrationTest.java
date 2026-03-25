package com.ledger.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.modules.auth.dto.LoginRequest;
import com.ledger.app.modules.auth.dto.RegisterRequest;
import com.ledger.app.modules.auth.dto.AuthResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.modules.budget.dto.CreateBudgetRequest;
import com.ledger.app.modules.budget.enums.BudgetPeriod;
import com.ledger.app.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算模块集成测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BudgetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long bookId;
    private Long budgetId;

    @BeforeEach
    void setUp() throws Exception {
        // 注册并登录
        String username = "budget_test_" + System.currentTimeMillis();
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username(username)
                .password("Test123456!")
                .email("budget_test@example.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password("Test123456!")
                .build();

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<AuthResponse> result = objectMapper.readValue(response, objectMapper.getTypeFactory()
                .constructParametricType(Result.class, AuthResponse.class));
        authToken = result.getData().getToken();

        // 创建账本
        CreateBookRequest bookRequest = CreateBookRequest.builder()
                .name("测试账本")
                .icon("wallet")
                .build();

        String bookResponse = mockMvc.perform(post("/api/book")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> bookResult = objectMapper.readValue(bookResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        bookId = bookResult.getData();

        // 创建预算
        CreateBudgetRequest budgetRequest = CreateBudgetRequest.builder()
                .bookId(bookId)
                .amount(new BigDecimal("5000.00"))
                .period(BudgetPeriod.MONTHLY.getCode())
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        String budgetResponse = mockMvc.perform(post("/api/budget")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Result<Long> budgetResult = objectMapper.readValue(budgetResponse,
                objectMapper.getTypeFactory().constructParametricType(Result.class, Long.class));
        budgetId = budgetResult.getData();
    }

    @Test
    void testCreateBudget() throws Exception {
        assert budgetId != null && budgetId > 0;
    }

    @Test
    void testGetBudgetById() throws Exception {
        mockMvc.perform(get("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(budgetId));
    }

    @Test
    void testGetBudgetsByBookId() throws Exception {
        mockMvc.perform(get("/api/budget")
                        .header("Authorization", "Bearer " + authToken)
                        .param("bookId", bookId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    void testUpdateBudget() throws Exception {
        CreateBudgetRequest updateRequest = CreateBudgetRequest.builder()
                .amount(new BigDecimal("6000.00"))
                .build();

        mockMvc.perform(put("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteBudget() throws Exception {
        mockMvc.perform(delete("/api/budget/" + budgetId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
