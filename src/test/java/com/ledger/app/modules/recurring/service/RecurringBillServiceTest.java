package com.ledger.app.modules.recurring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.recurring.dto.request.CreateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.request.UpdateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.response.RecurringBillResponse;
import com.ledger.app.modules.recurring.entity.RecurringBill;
import com.ledger.app.modules.recurring.enums.RecurringStatus;
import com.ledger.app.modules.recurring.enums.RecurringType;
import com.ledger.app.modules.recurring.repository.RecurringBillRepository;
import com.ledger.app.modules.recurring.service.impl.RecurringBillServiceImpl;
import com.ledger.app.modules.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 周期账单服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("周期账单 Service 测试")
class RecurringBillServiceTest {

    @Mock
    private RecurringBillRepository recurringBillRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RecurringBillServiceImpl recurringBillService;

    private CreateRecurringBillRequest createRequest;
    private RecurringBill bill;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;

        createRequest = CreateRecurringBillRequest.builder()
                .bookId(1L)
                .name("每月房租")
                .recurringType(4) // 每月
                .recurringValue(1)
                .amount(new BigDecimal("3000.00"))
                .categoryId(1L)
                .accountId(1L)
                .transactionType(2)
                .note("每月房租")
                .merchant("房东")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(null)
                .maxExecutions(null)
                .autoExecute(true)
                .build();

        bill = RecurringBill.builder()
                .id(1L)
                .bookId(1L)
                .userId(userId)
                .name("每月房租")
                .recurringType(4)
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
                .status(RecurringStatus.ACTIVE.getCode())
                .autoExecute(true)
                .executionCount(0)
                .deleted(0)
                .build();
    }

    @Test
    @DisplayName("创建周期账单 - 成功")
    void create_Success() {
        // Arrange
        when(recurringBillRepository.insert(any(RecurringBill.class))).thenReturn(1);

        // Act
        Long result = recurringBillService.create(createRequest, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(recurringBillRepository, times(1)).insert(any(RecurringBill.class));
    }

    @Test
    @DisplayName("创建周期账单 - 结束日期早于开始日期")
    void create_EndDateBeforeStartDate() {
        // Arrange
        createRequest.setEndDate(LocalDate.of(2026, 2, 1)); // 早于开始日期

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            recurringBillService.create(createRequest, userId);
        });
        assertTrue(exception.getMessage().contains("结束日期不能早于开始日期"));
        verify(recurringBillRepository, never()).insert(any());
    }

    @Test
    @DisplayName("更新周期账单 - 成功")
    void update_Success() {
        // Arrange
        UpdateRecurringBillRequest updateRequest = UpdateRecurringBillRequest.builder()
                .name("每月房租 - 更新")
                .amount(new BigDecimal("3200.00"))
                .build();

        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any(RecurringBill.class))).thenReturn(1);

        // Act
        recurringBillService.update(1L, updateRequest, userId);

        // Assert
        verify(recurringBillRepository, times(1)).selectById(1L);
        verify(recurringBillRepository, times(1)).updateById(any(RecurringBill.class));
    }

    @Test
    @DisplayName("更新周期账单 - 账单不存在")
    void update_BillNotFound() {
        // Arrange
        UpdateRecurringBillRequest updateRequest = UpdateRecurringBillRequest.builder()
                .name("更新名称")
                .build();

        when(recurringBillRepository.selectById(1L)).thenReturn(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            recurringBillService.update(1L, updateRequest, userId);
        });
        assertTrue(exception.getMessage().contains("周期账单不存在"));
    }

    @Test
    @DisplayName("更新周期账单 - 无权访问")
    void update_Unauthorized() {
        // Arrange
        UpdateRecurringBillRequest updateRequest = UpdateRecurringBillRequest.builder()
                .name("更新名称")
                .build();

        RecurringBill otherUserBill = RecurringBill.builder()
                .id(1L)
                .userId(999L) // 其他用户的账单
                .deleted(0)
                .build();

        when(recurringBillRepository.selectById(1L)).thenReturn(otherUserBill);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            recurringBillService.update(1L, updateRequest, userId);
        });
        assertTrue(exception.getMessage().contains("无权访问"));
    }

    @Test
    @DisplayName("删除周期账单 - 成功")
    void delete_Success() {
        // Arrange
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.deleteById(1L)).thenReturn(1);

        // Act
        recurringBillService.delete(1L, userId);

        // Assert
        verify(recurringBillRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("获取周期账单详情 - 成功")
    void getById_Success() {
        // Arrange
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);

        // Act
        RecurringBillResponse result = recurringBillService.getById(1L, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("每月房租", result.getName());
        assertEquals(new BigDecimal("3000.00"), result.getAmount());
    }

    @Test
    @DisplayName("分页查询周期账单列表 - 成功")
    void pageByBookId_Success() {
        // Arrange
        Page<RecurringBill> billPage = new Page<>(1, 20, 2);
        billPage.setRecords(Arrays.asList(bill, bill));
        billPage.setTotal(2);

        when(recurringBillRepository.selectPage(any(), any())).thenReturn(billPage);

        // Act
        Page<RecurringBillResponse> result = recurringBillService.pageByBookId(1L, userId, 1, 20);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
    }

    @Test
    @DisplayName("手动执行周期账单 - 成功")
    void execute_Success() {
        // Arrange
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any(RecurringBill.class))).thenReturn(1);
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        // Act
        recurringBillService.execute(1L, userId);

        // Assert
        verify(transactionService, times(1)).create(any(), eq(userId));
        verify(recurringBillRepository, times(1)).updateById(any(RecurringBill.class));
    }

    @Test
    @DisplayName("暂停周期账单 - 成功")
    void pause_Success() {
        // Arrange
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any(RecurringBill.class))).thenReturn(1);

        // Act
        recurringBillService.pause(1L, userId);

        // Assert
        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getStatus().equals(RecurringStatus.PAUSED.getCode())
        ));
    }

    @Test
    @DisplayName("恢复周期账单 - 成功")
    void resume_Success() {
        // Arrange
        bill.setStatus(RecurringStatus.PAUSED.getCode());
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any(RecurringBill.class))).thenReturn(1);

        // Act
        recurringBillService.resume(1L, userId);

        // Assert
        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getStatus().equals(RecurringStatus.ACTIVE.getCode())
        ));
    }

    @Test
    @DisplayName("跳过本次执行 - 成功")
    void skip_Success() {
        // Arrange
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any(RecurringBill.class))).thenReturn(1);

        // Act
        recurringBillService.skip(1L, userId);

        // Assert
        verify(recurringBillRepository, times(1)).updateById(any(RecurringBill.class));
    }

    @Test
    @DisplayName("自动执行待执行账单 - 成功")
    void autoExecutePending_Success() {
        // Arrange
        RecurringBill bill1 = RecurringBill.builder()
                .id(1L)
                .userId(userId)
                .recurringType(4)
                .recurringValue(1)
                .amount(new BigDecimal("100.00"))
                .categoryId(1L)
                .accountId(1L)
                .transactionType(2)
                .bookId(1L)
                .nextExecutionDate(LocalDate.now().minusDays(1))
                .status(RecurringStatus.ACTIVE.getCode())
                .autoExecute(true)
                .executionCount(0)
                .deleted(0)
                .build();

        when(recurringBillRepository.selectPendingExecutions(any(LocalDate.class)))
                .thenReturn(Arrays.asList(bill1));
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        // Act
        int result = recurringBillService.autoExecutePending();

        // Assert
        assertEquals(1, result);
        verify(transactionService, times(1)).create(any(), anyLong());
    }

    @Test
    @DisplayName("计算下次执行日期 - 每日")
    void calculateNextExecutionDate_Daily() {
        // Arrange
        bill.setRecurringType(RecurringType.DAILY.getCode());

        // Act & Assert (通过执行来验证)
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any())).thenReturn(1);
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        recurringBillService.execute(1L, userId);

        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getNextExecutionDate().equals(LocalDate.of(2026, 3, 2))
        ));
    }

    @Test
    @DisplayName("计算下次执行日期 - 每周")
    void calculateNextExecutionDate_Weekly() {
        // Arrange
        bill.setRecurringType(RecurringType.WEEKLY.getCode());
        bill.setRecurringValue(1);

        // Act & Assert
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any())).thenReturn(1);
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        recurringBillService.execute(1L, userId);

        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getNextExecutionDate().equals(LocalDate.of(2026, 3, 8))
        ));
    }

    @Test
    @DisplayName("计算下次执行日期 - 每月")
    void calculateNextExecutionDate_Monthly() {
        // Arrange
        bill.setRecurringType(RecurringType.MONTHLY.getCode());
        bill.setRecurringValue(1);

        // Act & Assert
        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any())).thenReturn(1);
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        recurringBillService.execute(1L, userId);

        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getNextExecutionDate().equals(LocalDate.of(2026, 4, 1))
        ));
    }

    @Test
    @DisplayName("周期账单完成 - 达到最大执行次数")
    void isCompleted_MaxExecutionsReached() {
        // Arrange
        bill.setMaxExecutions(3);
        bill.setExecutionCount(2);

        when(recurringBillRepository.selectById(1L)).thenReturn(bill);
        when(recurringBillRepository.updateById(any())).thenReturn(1);
        when(transactionService.create(any(), anyLong())).thenReturn(1L);

        // Act
        recurringBillService.execute(1L, userId);

        // Assert
        verify(recurringBillRepository, times(1)).updateById(argThat(b -> 
            b.getStatus().equals(RecurringStatus.COMPLETED.getCode())
        ));
    }
}
