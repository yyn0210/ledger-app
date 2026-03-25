package com.ledger.app.modules.recurring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.app.common.result.PageResult;
import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.recurring.dto.request.CreateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.request.UpdateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.response.RecurringBillResponse;
import com.ledger.app.modules.recurring.service.RecurringBillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 周期账单控制器单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@WebMvcTest(RecurringBillController.class)
@DisplayName("周期账单 Controller 测试")
class RecurringBillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecurringBillService recurringBillService;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateRecurringBillRequest createRequest;
    private UpdateRecurringBillRequest updateRequest;
    private RecurringBillResponse billResponse;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        createRequest = CreateRecurringBillRequest.builder()
                .bookId(1L)
                .name("每月房租")
                .recurringType(4) // 每月
                .recurringValue(1)
                .amount(new BigDecimal("3000.00"))
                .categoryId(1L)
                .accountId(1L)
                .transactionType(2) // 支出
                .note("每月房租")
                .merchant("房东")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(null)
                .maxExecutions(null)
                .autoExecute(true)
                .build();

        updateRequest = UpdateRecurringBillRequest.builder()
                .name("每月房租 - 更新")
                .amount(new BigDecimal("3200.00"))
                .build();

        billResponse = RecurringBillResponse.builder()
                .id(1L)
                .bookId(1L)
                .name("每月房租")
                .recurringTypeName("每月")
                .recurringValue(1)
                .amount(new BigDecimal("3000.00"))
                .categoryId(1L)
                .accountId(1L)
                .transactionType(2)
                .note("每月房租")
                .merchant("房东")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(null)
                .nextExecutionDate(LocalDate.of(2026, 3, 1))
                .lastExecutionDate(null)
                .executionCount(0)
                .maxExecutions(null)
                .status(1)
                .statusName("执行中")
                .autoExecute(true)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    @Test
    @DisplayName("创建周期账单 - 成功")
    void create_Success() throws Exception {
        // Arrange
        when(recurringBillService.create(any(CreateRecurringBillRequest.class), anyLong()))
                .thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(recurringBillService, times(1)).create(any(CreateRecurringBillRequest.class), anyLong());
    }

    @Test
    @DisplayName("创建周期账单 - 账单名称为空")
    void create_NameEmpty() throws Exception {
        // Arrange
        createRequest.setName("");

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());

        verify(recurringBillService, never()).create(any(), anyLong());
    }

    @Test
    @DisplayName("创建周期账单 - 金额为空")
    void create_AmountEmpty() throws Exception {
        // Arrange
        createRequest.setAmount(null);

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());

        verify(recurringBillService, never()).create(any(), anyLong());
    }

    @Test
    @DisplayName("创建周期账单 - 周期类型为空")
    void create_RecurringTypeEmpty() throws Exception {
        // Arrange
        createRequest.setRecurringType(null);

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());

        verify(recurringBillService, never()).create(any(), anyLong());
    }

    @Test
    @DisplayName("更新周期账单 - 成功")
    void update_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).update(anyLong(), any(UpdateRecurringBillRequest.class), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/recurring-bill/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).update(eq(1L), any(UpdateRecurringBillRequest.class), anyLong());
    }

    @Test
    @DisplayName("删除周期账单 - 成功")
    void delete_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).delete(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/recurring-bill/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).delete(eq(1L), anyLong());
    }

    @Test
    @DisplayName("获取周期账单详情 - 成功")
    void getById_Success() throws Exception {
        // Arrange
        when(recurringBillService.getById(anyLong(), anyLong())).thenReturn(billResponse);

        // Act & Assert
        mockMvc.perform(get("/api/recurring-bill/1")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("每月房租"))
                .andExpect(jsonPath("$.data.amount").value(3000.00));

        verify(recurringBillService, times(1)).getId(eq(1L), anyLong());
    }

    @Test
    @DisplayName("分页查询周期账单列表 - 成功")
    void pageByBookId_Success() throws Exception {
        // Arrange
        Page<RecurringBillResponse> page = new Page<>(1, 20, 1);
        page.setRecords(java.util.Collections.singletonList(billResponse));
        page.setTotal(1);

        when(recurringBillService.pageByBookId(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/recurring-bill")
                        .param("bookId", "1")
                        .param("page", "1")
                        .param("size", "20")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(20));

        verify(recurringBillService, times(1)).pageByBookId(eq(1L), anyLong(), eq(1), eq(20));
    }

    @Test
    @DisplayName("手动执行周期账单 - 成功")
    void execute_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).execute(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill/1/execute")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).execute(eq(1L), anyLong());
    }

    @Test
    @DisplayName("暂停周期账单 - 成功")
    void pause_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).pause(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill/1/pause")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).pause(eq(1L), anyLong());
    }

    @Test
    @DisplayName("恢复周期账单 - 成功")
    void resume_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).resume(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill/1/resume")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).resume(eq(1L), anyLong());
    }

    @Test
    @DisplayName("跳过本次执行 - 成功")
    void skip_Success() throws Exception {
        // Arrange
        doNothing().when(recurringBillService).skip(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/recurring-bill/1/skip")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recurringBillService, times(1)).skip(eq(1L), anyLong());
    }
}
