package com.ledger.app.modules.transaction.repository;

import com.ledger.app.modules.transaction.entity.Transaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录 Mapper 接口
 */
@Mapper
public interface TransactionRepository extends BaseMapper<Transaction> {

    /**
     * 分页查询交易记录
     *
     * @param page         分页对象
     * @param bookId       账本 ID
     * @param type         交易类型（可选）
     * @param categoryId   分类 ID（可选）
     * @param accountId    账户 ID（可选）
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param minAmount    最小金额（可选）
     * @param maxAmount    最大金额（可选）
     * @param keyword      关键词（可选）
     * @return 分页结果
     */
    IPage<Transaction> selectPageWithFilters(
            Page<Transaction> page,
            @Param("bookId") Long bookId,
            @Param("type") Integer type,
            @Param("categoryId") Long categoryId,
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("keyword") String keyword
    );

    /**
     * 根据 ID 和账本 ID 获取交易
     *
     * @param id     交易 ID
     * @param bookId 账本 ID
     * @return 交易记录
     */
    @Select("SELECT * FROM transactions WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Transaction selectByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计指定账户的交易记录数量
     *
     * @param accountId 账户 ID
     * @return 交易记录数量
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE account_id = #{accountId} AND deleted = 0")
    Long countByAccountId(@Param("accountId") Long accountId);

    /**
     * 批量插入交易记录
     *
     * @param transactions 交易记录列表
     * @return 插入数量
     */
    int insertBatch(@Param("list") List<Transaction> transactions);

    /**
     * 增加账户余额
     *
     * @param accountId 账户 ID
     * @param amount    金额
     * @return 影响行数
     */
    @Update("UPDATE accounts SET balance = balance + #{amount}, updated_at = NOW() WHERE id = #{accountId}")
    int increaseBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);

    /**
     * 减少账户余额
     *
     * @param accountId 账户 ID
     * @param amount    金额
     * @return 影响行数
     */
    @Update("UPDATE accounts SET balance = balance - #{amount}, updated_at = NOW() WHERE id = #{accountId}")
    int decreaseBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}
