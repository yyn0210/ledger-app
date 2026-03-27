package com.ledger.app.modules.recurring.entity;

import com.ledger.app.modules.recurring.enums.ExecutionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期账单执行记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringBillExecution {
    private Long id;
    private Long recurringBillId;
    private LocalDate scheduledDate;
    private LocalDate actualDate;
    private Long transactionId;
    private ExecutionStatus status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
