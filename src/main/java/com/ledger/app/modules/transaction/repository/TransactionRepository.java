package com.ledger.app.modules.transaction.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.app.modules.transaction.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface TransactionRepository extends BaseMapper<Transaction> {

    /**
     * 分页查询交易记录（带复杂筛选）
     *
     * @param page 分页对象
     * @param bookId 账本 ID
     * @param type 交易类型（null 表示全部）
     * @param categoryId 分类 ID（null 表示不限）
     * @param accountId 账户 ID（null 表示不限）
     * @param startDate 开始日期（null 表示不限）
     * @param endDate 结束日期（null 表示不限）
     * @param minAmount 最小金额（null 表示不限）
     * @param maxAmount 最大金额（null 表示不限）
     * @param keyword 关键词（null 表示不限）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM transactions WHERE book_id = #{bookId} AND deleted = 0 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "<if test='categoryId != null'>AND category_id = #{categoryId}</if> " +
            "<if test='accountId != null'>AND account_id = #{accountId}</if> " +
            "<if test='startDate != null'>AND transaction_date &gt;= #{startDate}</if> " +
            "<if test='endDate != null'>AND transaction_date &lt;= #{endDate}</if> " +
            "<if test='minAmount != null'>AND amount &gt;= #{minAmount}</if> " +
            "<if test='maxAmount != null'>AND amount &lt;= #{maxAmount}</if> " +
            "<if test='keyword != null'>AND (title LIKE CONCAT('%', #{keyword}, '%') OR description LIKE CONCAT('%', #{keyword}, '%'))</if> " +
            "ORDER BY transaction_date DESC, created_at DESC" +
            "</script>")
    IPage<Transaction> selectPageWithFilter(Page<Transaction> page,
                                             @Param("bookId") Long bookId,
                                             @Param("type") Integer type,
                                             @Param("categoryId") Long categoryId,
                                             @Param("accountId") Long accountId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             @Param("minAmount") BigDecimal minAmount,
                                             @Param("maxAmount") BigDecimal maxAmount,
                                             @Param("keyword") String keyword);

    /**
     * 根据 ID 和账本 ID 获取交易记录（权限检查）
     *
     * @param id 交易 ID
     * @param bookId 账本 ID
     * @return 交易记录
     */
    @Select("SELECT * FROM transactions WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Transaction findByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计交易数量
     *
     * @param bookId 账本 ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE book_id = #{bookId} AND deleted = 0")
    int countByBookId(@Param("bookId") Long bookId);

    /**
     * 增加账户余额
     *
     * @param accountId 账户 ID
     * @param amount 金额
     * @return 影响行数
     */
    @Update("UPDATE accounts SET balance = balance + #{amount}, updated_at = NOW() WHERE id = #{accountId}")
    int increaseBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * 减少账户余额
     *
     * @param accountId 账户 ID
     * @param amount 金额
     * @return 影响行数
     */
    @Update("UPDATE accounts SET balance = balance - #{amount}, updated_at = NOW() WHERE id = #{accountId}")
    int decreaseBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * 批量插入交易记录
     *
     * @param transactions 交易记录列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<Transaction> transactions);
}
