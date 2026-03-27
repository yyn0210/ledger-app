package com.ledger.app.modules.account.repository;

import com.ledger.app.modules.account.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户 Mapper 接口
 */
@Mapper
public interface AccountRepository extends BaseMapper<Account> {

    /**
     * 根据账本 ID 获取账户列表
     *
     * @param bookId 账本 ID
     * @return 账户列表
     */
    @Select("SELECT * FROM accounts WHERE book_id = #{bookId} AND deleted = 0 ORDER BY sort_order, id")
    List<Account> selectByBookId(@Param("bookId") Long bookId);

    /**
     * 根据账本 ID 和用户 ID 获取账户列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 账户列表
     */
    @Select("SELECT * FROM accounts WHERE book_id = #{bookId} AND user_id = #{userId} AND deleted = 0 ORDER BY sort_order, id")
    List<Account> selectByBookIdAndUserId(@Param("bookId") Long bookId, @Param("userId") Long userId);

    /**
     * 根据 ID 和账本 ID 获取账户
     *
     * @param id     账户 ID
     * @param bookId 账本 ID
     * @return 账户
     */
    @Select("SELECT * FROM accounts WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Account selectByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计账户总余额（只计算 is_include=true 的账户）
     *
     * @param userId 用户 ID
     * @param include 是否只统计计入总资产的账户
     * @return 总余额
     */
    @Select("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE user_id = #{userId} AND is_include = #{include} AND deleted = 0")
    BigDecimal sumByUserIdAndInclude(@Param("userId") Long userId, @Param("include") boolean include);

    /**
     * 按账户类型分组统计余额
     *
     * @param userId 用户 ID
     * @return 类型和余额列表 [type, balance]
     */
    @Select("SELECT type, SUM(balance) as balance FROM accounts WHERE user_id = #{userId} AND deleted = 0 GROUP BY type")
    List<AccountTypeBalance> sumByType(@Param("userId") Long userId);

    /**
     * 统计交易记录中使用此账户的数量
     *
     * @param accountId 账户 ID
     * @return 交易记录数量
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE account_id = #{accountId} AND deleted = 0")
    Long countTransactionsByAccountId(@Param("accountId") Long accountId);

    /**
     * 账户类型余额 DTO
     */
    interface AccountTypeBalance {
        Integer getType();
        BigDecimal getBalance();
    }
}
