package com.ledger.app.modules.budget.repository;

import com.ledger.app.modules.budget.entity.Budget;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 预算 Mapper 接口
 */
@Mapper
public interface BudgetRepository extends BaseMapper<Budget> {

    /**
     * 根据账本 ID 和用户 ID 获取预算列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 预算列表
     */
    @Select("SELECT * FROM budgets WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Budget> selectByBookIdAndUserId(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 根据 ID 和账本 ID 获取预算
     *
     * @param id     预算 ID
     * @param bookId 账本 ID
     * @return 预算
     */
    @Select("SELECT * FROM budgets WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Budget selectByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计预算周期内、该分类的实际支出
     *
     * @param bookId     账本 ID
     * @param categoryId 分类 ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 支出总额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE book_id = #{bookId} AND category_id = #{categoryId} AND type = 2 AND transaction_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    BigDecimal sumByBookIdAndCategoryIdAndDateRange(
            @Param("bookId") Long bookId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计预算周期内的总支出（不限制分类）
     *
     * @param bookId    账本 ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 支出总额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE book_id = #{bookId} AND type = 2 AND transaction_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0")
    BigDecimal sumTotalExpenseByDateRange(
            @Param("bookId") Long bookId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 按周期过滤预算
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期
     * @return 预算列表
     */
    @Select("SELECT * FROM budgets WHERE book_id = #{bookId} AND user_id = #{userId} AND period = #{period} AND deleted = 0 ORDER BY created_at DESC")
    List<Budget> selectByBookIdAndUserIdAndPeriod(
            @Param("bookId") Long bookId,
            @Param("userId") Long userId,
            @Param("period") String period
    );

    /**
     * 按状态过滤预算
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param status 状态
     * @return 预算列表
     */
    @Select("SELECT * FROM budgets WHERE book_id = #{bookId} AND user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Budget> selectByBookIdAndUserIdAndStatus(
            @Param("bookId") Long bookId,
            @Param("userId") Long userId,
            @Param("status") String status
    );
}
