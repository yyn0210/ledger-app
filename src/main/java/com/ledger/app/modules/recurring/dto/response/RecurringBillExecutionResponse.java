package com.ledger.app.modules.recurring.dto.response;

import com.ledger.app.modules.recurring.enums.ExecutionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 周期账单执行记录响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringBillExecutionResponse {
    private Long id;
    private Long recurringBillId;
    private String billName;
    private LocalDate scheduledDate;
    private LocalDate actualDate;
    private Long transactionId;
    private ExecutionStatus status;
    private String statusDescription;
    private String errorMessage;
    private LocalDateTime createdAt;
}
