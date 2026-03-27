package com.ledger.app.modules.recurring.repository;

import com.ledger.app.modules.recurring.entity.RecurringBillExecution;
import com.ledger.app.modules.recurring.enums.ExecutionStatus;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 周期账单执行记录 Mapper
 */
@Mapper
public interface RecurringBillExecutionRepository {

    @Insert("INSERT INTO recurring_bill_executions (recurring_bill_id, scheduled_date, actual_date, " +
            "transaction_id, status, error_message, created_at) " +
            "VALUES (#{recurringBillId}, #{scheduledDate}, #{actualDate}, #{transactionId}, #{status}, #{errorMessage}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecurringBillExecution execution);

    @Update("UPDATE recurring_bill_executions SET actual_date=#{actualDate}, transaction_id=#{transactionId}, " +
            "status=#{status}, error_message=#{errorMessage} WHERE id=#{id}")
    int update(RecurringBillExecution execution);

    @Select("SELECT * FROM recurring_bill_executions WHERE id=#{id}")
    @Results(id = "executionMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "recurringBillId", column = "recurring_bill_id"),
        @Result(property = "scheduledDate", column = "scheduled_date"),
        @Result(property = "actualDate", column = "actual_date"),
        @Result(property = "transactionId", column = "transaction_id"),
        @Result(property = "status", column = "status", javaType = ExecutionStatus.class),
        @Result(property = "errorMessage", column = "error_message"),
        @Result(property = "createdAt", column = "created_at")
    })
    RecurringBillExecution findById(@Param("id") Long id);

    @Select("SELECT * FROM recurring_bill_executions WHERE recurring_bill_id=#{recurringBillId} ORDER BY scheduled_date DESC")
    @ResultMap("executionMap")
    List<RecurringBillExecution> findByRecurringBillId(@Param("recurringBillId") Long recurringBillId);

    @Select("SELECT * FROM recurring_bill_executions WHERE recurring_bill_id=#{recurringBillId} " +
            "AND scheduled_date=#{scheduledDate} LIMIT 1")
    @ResultMap("executionMap")
    RecurringBillExecution findByBillIdAndDate(@Param("recurringBillId") Long recurringBillId, @Param("scheduledDate") LocalDate scheduledDate);

    @Select("SELECT * FROM recurring_bill_executions WHERE status='PENDING' AND scheduled_date &lt;= #{date}")
    @ResultMap("executionMap")
    List<RecurringBillExecution> findPendingExecutions(@Param("date") LocalDate date);
}
