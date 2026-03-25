package com.ledger.app.modules.recurring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.recurring.dto.request.CreateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.request.UpdateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.response.RecurringBillResponse;
import com.ledger.app.modules.recurring.entity.RecurringBill;
import com.ledger.app.modules.recurring.enums.RecurringStatus;
import com.ledger.app.modules.recurring.enums.RecurringType;
import com.ledger.app.modules.recurring.repository.RecurringBillRepository;
import com.ledger.app.modules.recurring.service.RecurringBillService;
import com.ledger.app.modules.transaction.dto.request.CreateTransactionRequest;
import com.ledger.app.modules.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 周期账单服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringBillServiceImpl implements RecurringBillService {

    private final RecurringBillRepository recurringBillRepository;
    private final TransactionService transactionService;

    @Override
    @Transactional
    public Long create(CreateRecurringBillRequest request, Long userId) {
        // 验证结束日期
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        // 创建周期账单
        RecurringBill bill = RecurringBill.builder()
                .bookId(request.getBookId())
                .userId(userId)
                .name(request.getName())
                .recurringType(request.getRecurringType())
                .recurringValue(request.getRecurringValue())
                .amount(request.getAmount())
                .categoryId(request.getCategoryId())
                .accountId(request.getAccountId())
                .transactionType(request.getTransactionType())
                .note(request.getNote())
                .merchant(request.getMerchant())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(request.getStartDate())
                .status(RecurringStatus.ACTIVE.getCode())
                .autoExecute(request.getAutoExecute())
                .maxExecutions(request.getMaxExecutions())
                .executionCount(0)
                .build();

        recurringBillRepository.insert(bill);
        log.info("Created recurring bill: id={}, name={}", bill.getId(), bill.getName());

        return bill.getId();
    }

    @Override
    @Transactional
    public void update(Long id, UpdateRecurringBillRequest request, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);

        // 更新字段
        if (request.getName() != null) {
            bill.setName(request.getName());
        }
        if (request.getAmount() != null) {
            bill.setAmount(request.getAmount());
        }
        if (request.getCategoryId() != null) {
            bill.setCategoryId(request.getCategoryId());
        }
        if (request.getAccountId() != null) {
            bill.setAccountId(request.getAccountId());
        }
        if (request.getNote() != null) {
            bill.setNote(request.getNote());
        }
        if (request.getMerchant() != null) {
            bill.setMerchant(request.getMerchant());
        }
        if (request.getEndDate() != null) {
            bill.setEndDate(request.getEndDate());
        }
        if (request.getMaxExecutions() != null) {
            bill.setMaxExecutions(request.getMaxExecutions());
        }
        if (request.getAutoExecute() != null) {
            bill.setAutoExecute(request.getAutoExecute());
        }
        if (request.getStatus() != null) {
            bill.setStatus(request.getStatus());
        }

        recurringBillRepository.updateById(bill);
        log.info("Updated recurring bill: id={}", id);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        recurringBillRepository.deleteById(id);
        log.info("Deleted recurring bill: id={}", id);
    }

    @Override
    public RecurringBillResponse getById(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        return convertToResponse(bill);
    }

    @Override
    public Page<RecurringBillResponse> pageByBookId(Long bookId, Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<RecurringBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecurringBill::getBookId, bookId)
                .eq(RecurringBill::getUserId, userId)
                .eq(RecurringBill::getDeleted, 0)
                .orderByDesc(RecurringBill::getCreatedAt);

        Page<RecurringBill> billPage = recurringBillRepository.selectPage(
                new Page<>(page, size), wrapper);

        List<RecurringBillResponse> responses = billPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        Page<RecurringBillResponse> responsePage = new Page<>(page, size, billPage.getTotal());
        responsePage.setRecords(responses);
        return responsePage;
    }

    @Override
    @Transactional
    public void execute(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        executeBill(bill);
        log.info("Manually executed recurring bill: id={}", id);
    }

    @Override
    @Transactional
    public void pause(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        bill.setStatus(RecurringStatus.PAUSED.getCode());
        recurringBillRepository.updateById(bill);
        log.info("Paused recurring bill: id={}", id);
    }

    @Override
    @Transactional
    public void resume(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        bill.setStatus(RecurringStatus.ACTIVE.getCode());
        recurringBillRepository.updateById(bill);
        log.info("Resumed recurring bill: id={}", id);
    }

    @Override
    @Transactional
    public void skip(Long id, Long userId) {
        RecurringBill bill = getByIdEntity(id, userId);
        // 计算下次执行日期
        LocalDate nextDate = calculateNextExecutionDate(bill, bill.getNextExecutionDate());
        bill.setNextExecutionDate(nextDate);
        recurringBillRepository.updateById(bill);
        log.info("Skipped recurring bill execution: id={}, nextDate={}", id, nextDate);
    }

    @Override
    @Transactional
    public int autoExecutePending() {
        LocalDate today = LocalDate.now();
        List<RecurringBill> pendingBills = recurringBillRepository.selectPendingExecutions(today);

        int executedCount = 0;
        for (RecurringBill bill : pendingBills) {
            try {
                executeBill(bill);
                executedCount++;
            } catch (Exception e) {
                log.error("Failed to auto-execute recurring bill: id={}, error={}", bill.getId(), e.getMessage());
            }
        }

        if (executedCount > 0) {
            log.info("Auto-executed {} recurring bills", executedCount);
        }
        return executedCount;
    }

    /**
     * 执行周期账单
     */
    private void executeBill(RecurringBill bill) {
        // 创建交易记录
        CreateTransactionRequest txRequest = CreateTransactionRequest.builder()
                .bookId(bill.getBookId())
                .categoryId(bill.getCategoryId())
                .accountId(bill.getAccountId())
                .type(bill.getTransactionType())
                .amount(bill.getAmount())
                .transactionDate(bill.getNextExecutionDate())
                .note(bill.getNote() != null ? bill.getNote() + " [周期账单]" : "[周期账单]")
                .merchant(bill.getMerchant())
                .build();

        transactionService.create(txRequest, bill.getUserId());

        // 更新执行信息
        bill.setLastExecutionDate(bill.getNextExecutionDate());
        bill.setExecutionCount(bill.getExecutionCount() + 1);

        // 计算下次执行日期
        LocalDate nextDate = calculateNextExecutionDate(bill, bill.getNextExecutionDate());
        bill.setNextExecutionDate(nextDate);

        // 检查是否完成
        if (isCompleted(bill)) {
            bill.setStatus(RecurringStatus.COMPLETED.getCode());
        }

        recurringBillRepository.updateById(bill);
    }

    /**
     * 计算下次执行日期
     */
    private LocalDate calculateNextExecutionDate(RecurringBill bill, LocalDate currentDate) {
        RecurringType type = RecurringType.fromCode(bill.getRecurringType());
        Integer value = bill.getRecurringValue();

        switch (type) {
            case DAILY:
                return currentDate.plusDays(1);
            case WEEKLY:
                return currentDate.plusWeeks(value != null ? value : 1);
            case BIWEEKLY:
                return currentDate.plusWeeks(2);
            case MONTHLY:
                return currentDate.plusMonths(value != null ? value : 1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            case YEARLY:
                return currentDate.plusYears(value != null ? value : 1);
            default:
                return currentDate.plusDays(1);
        }
    }

    /**
     * 检查是否完成
     */
    private boolean isCompleted(RecurringBill bill) {
        // 检查是否超过结束日期
        if (bill.getEndDate() != null && bill.getNextExecutionDate().isAfter(bill.getEndDate())) {
            return true;
        }
        // 检查是否达到最大执行次数
        if (bill.getMaxExecutions() != null && bill.getExecutionCount() >= bill.getMaxExecutions()) {
            return true;
        }
        return false;
    }

    /**
     * 获取周期账单实体（带权限验证）
     */
    private RecurringBill getByIdEntity(Long id, Long userId) {
        RecurringBill bill = recurringBillRepository.selectById(id);
        if (bill == null || bill.getDeleted() != 0) {
            throw new BusinessException("周期账单不存在");
        }
        if (!bill.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该周期账单");
        }
        return bill;
    }

    /**
     * 转换为响应对象
     */
    private RecurringBillResponse convertToResponse(RecurringBill bill) {
        RecurringType type = RecurringType.fromCode(bill.getRecurringType());
        RecurringStatus status = RecurringStatus.fromCode(bill.getStatus());

        return RecurringBillResponse.builder()
                .id(bill.getId())
                .bookId(bill.getBookId())
                .name(bill.getName())
                .recurringTypeName(type.getName())
                .recurringValue(bill.getRecurringValue())
                .amount(bill.getAmount())
                .categoryId(bill.getCategoryId())
                .accountId(bill.getAccountId())
                .transactionType(bill.getTransactionType())
                .note(bill.getNote())
                .merchant(bill.getMerchant())
                .startDate(bill.getStartDate())
                .endDate(bill.getEndDate())
                .nextExecutionDate(bill.getNextExecutionDate())
                .lastExecutionDate(bill.getLastExecutionDate())
                .executionCount(bill.getExecutionCount())
                .maxExecutions(bill.getMaxExecutions())
                .status(bill.getStatus())
                .statusName(status.getName())
                .autoExecute(bill.getAutoExecute())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }
}
