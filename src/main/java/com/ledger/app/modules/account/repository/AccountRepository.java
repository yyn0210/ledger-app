package com.ledger.app.modules.account.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.account.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface AccountRepository extends BaseMapper<Account> {

    /**
     * 根据账本 ID 获取账户列表（按排序顺序）
     *
     * @param bookId 账本 ID
     * @return 账户列表
     */
    @Select("SELECT * FROM accounts WHERE book_id = #{bookId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Account> findByBookId(@Param("bookId") Long bookId);

    /**
     * 根据 ID 和账本 ID 获取账户（权限检查）
     *
     * @param id 账户 ID
     * @param bookId 账本 ID
     * @return 账户信息
     */
    @Select("SELECT * FROM accounts WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Account findByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计用户总资产（只计算 is_include=1 的账户）
     *
     * @param userId 用户 ID
     * @return 总资产
     */
    @Select("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE user_id = #{userId} AND is_include = 1 AND deleted = 0")
    BigDecimal sumTotalAssets(@Param("userId") Long userId);

    /**
     * 按账户类型统计余额
     *
     * @param userId 用户 ID
     * @return 类型和余额列表
     */
    @Select("SELECT type, SUM(balance) as balance FROM accounts WHERE user_id = #{userId} AND is_include = 1 AND deleted = 0 GROUP BY type")
    List<TypeBalance> sumByType(@Param("userId") Long userId);

    /**
     * 统计账户数量
     *
     * @param bookId 账本 ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM accounts WHERE book_id = #{bookId} AND deleted = 0")
    int countByBookId(@Param("bookId") Long bookId);

    /**
     * 类型余额 DTO
     */
    interface TypeBalance {
        Integer getType();
        BigDecimal getBalance();
    }

    /**
     * 增加账户余额
     */
    @org.apache.ibatis.annotations.Update("UPDATE accounts SET balance = balance + #{amount} WHERE id = #{id} AND deleted = 0")
    int increaseBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    /**
     * 减少账户余额
     */
    @org.apache.ibatis.annotations.Update("UPDATE accounts SET balance = balance - #{amount} WHERE id = #{id} AND deleted = 0")
    int decreaseBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
