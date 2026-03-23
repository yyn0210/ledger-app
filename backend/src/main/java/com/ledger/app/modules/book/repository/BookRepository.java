package com.ledger.app.modules.book.repository;

import com.ledger.app.modules.book.entity.Book;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 账本数据访问层
 */
@Mapper
public interface BookRepository extends BaseMapper<Book> {

    /**
     * 查询用户的所有账本（按排序顺序）
     */
    @Select("SELECT * FROM books WHERE user_id = #{userId} AND deleted = 0 ORDER BY sort_order, created_at DESC")
    List<Book> findByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的默认账本
     */
    @Select("SELECT * FROM books WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0 LIMIT 1")
    Book findDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据 ID 和用户 ID 查询账本（权限检查）
     */
    @Select("SELECT * FROM books WHERE id = #{id} AND user_id = #{userId} AND deleted = 0 LIMIT 1")
    Book findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 检查用户是否有默认账本
     */
    @Select("SELECT COUNT(*) FROM books WHERE user_id = #{userId} AND is_default = 1 AND deleted = 0")
    int countDefaultByUserId(@Param("userId") Long userId);
}
