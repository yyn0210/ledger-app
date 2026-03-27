package com.ledger.app.modules.recurring.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.recurring.entity.RecurringBill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 周期账单 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface RecurringBillRepository extends BaseMapper<RecurringBill> {

    /**
     * 查询待执行的周期账单
     *
     * @param date 日期
     * @return 待执行的周期账单列表
     */
    @Select("SELECT * FROM recurring_bill WHERE deleted = 0 AND status = 1 AND auto_execute = 1 " +
            "AND next_execution_date &lt;= #{date} ORDER BY next_execution_date ASC")
    List<RecurringBill> selectPendingExecutions(LocalDate date);

    /**
     * 查询用户的周期账单统计
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 统计信息
     */
    @Select("SELECT COUNT(*) as total, SUM(amount) as total_amount FROM recurring_bill " +
            "WHERE deleted = 0 AND book_id = #{bookId} AND user_id = #{userId}")
    RecurringBillStats selectStats(Long bookId, Long userId);

    /**
     * 统计信息实体
     */
    class RecurringBillStats {
        private Long total;
        private BigDecimal totalAmount;

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long total) {
            this.total = total;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
