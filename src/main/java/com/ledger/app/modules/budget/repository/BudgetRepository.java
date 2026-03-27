package com.ledger.app.modules.budget.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.budget.entity.Budget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 预算 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface BudgetRepository extends BaseMapper<Budget> {

    /**
     * 根据账本 ID 获取预算列表
     *
     * @param bookId 账本 ID
     * @param period 周期（null 表示全部）
     * @param status 状态（null 表示全部）
     * @return 预算列表
     */
    @Select("<script>" +
            "SELECT * FROM budgets WHERE book_id = #{bookId} AND deleted = 0 " +
            "<if test='period != null'>AND period = #{period}</if> " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Budget> findByBookId(@Param("bookId") Long bookId,
                               @Param("period") String period,
                               @Param("status") String status);

    /**
     * 根据 ID 和账本 ID 获取预算（权限检查）
     *
     * @param id 预算 ID
     * @param bookId 账本 ID
     * @return 预算信息
     */
    @Select("SELECT * FROM budgets WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Budget findByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计预算周期内、该分类的实际支出
     *
     * @param bookId 账本 ID
     * @param categoryId 分类 ID（null 表示所有分类）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总支出金额
     */
    @Select("<script>" +
            "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE book_id = #{bookId} AND deleted = 0 AND type = 2 " +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if> " +
            "AND transaction_date &gt;= #{startDate} AND transaction_date &lt;= #{endDate}" +
            "</script>")
    BigDecimal sumExpensesByBookIdAndCategoryIdAndDateRange(@Param("bookId") Long bookId,
                                                             @Param("categoryId") Long categoryId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    /**
     * 统计预算数量
     *
     * @param bookId 账本 ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM budgets WHERE book_id = #{bookId} AND deleted = 0")
    int countByBookId(@Param("bookId") Long bookId);
}
