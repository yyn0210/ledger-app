package com.ledger.app.modules.savings.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.savings.entity.SavingsGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 储蓄目标 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface SavingsGoalRepository extends BaseMapper<SavingsGoal> {

    /**
     * 查询用户的储蓄目标列表
     */
    @Select("SELECT * FROM savings_goal WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<SavingsGoal> findByBookIdAndUserId(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 查询进行中的储蓄目标
     */
    @Select("SELECT * FROM savings_goal WHERE book_id = #{bookId} AND user_id = #{userId} AND status = 1 AND deleted = 0 ORDER BY created_at DESC")
    List<SavingsGoal> findActiveByBookId(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 查询即将到期的储蓄目标（7 天内）
     */
    @Select("SELECT * FROM savings_goal WHERE book_id = #{bookId} AND user_id = #{userId} AND status = 1 AND target_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND deleted = 0 ORDER BY target_date ASC")
    List<SavingsGoal> findExpiringSoon(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 查询已完成的储蓄目标
     */
    @Select("SELECT * FROM savings_goal WHERE book_id = #{bookId} AND user_id = #{userId} AND status = 2 AND deleted = 0 ORDER BY updated_at DESC")
    List<SavingsGoal> findCompleted(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 统计总储蓄金额
     */
    @Select("SELECT COALESCE(SUM(saved_amount), 0) FROM savings_goal WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0")
    BigDecimal sumTotalSaved(@Param("bookId") Long bookId, @Param("userId") Long userId);
}
