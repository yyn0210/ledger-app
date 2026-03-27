package com.ledger.app.modules.category.repository;

import com.ledger.app.modules.category.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper 接口
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {

    /**
     * 根据账本 ID 和类型查询分类列表
     *
     * @param bookId 账本 ID
     * @param type   分类类型：1=支出 2=收入
     * @return 分类列表
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND type = #{type} AND deleted = 0 ORDER BY sort_order")
    List<Category> selectByBookIdAndType(@Param("bookId") Long bookId, @Param("type") Integer type);

    /**
     * 根据账本 ID 查询所有分类
     *
     * @param bookId 账本 ID
     * @return 分类列表
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND deleted = 0 ORDER BY type, sort_order")
    List<Category> selectByBookId(@Param("bookId") Long bookId);

    /**
     * 检查分类是否被交易记录引用
     *
     * @param categoryId 分类 ID
     * @return 引用数量
     */
    @Select("SELECT COUNT(*) FROM transactions WHERE category_id = #{categoryId} AND deleted = 0")
    Long countTransactionsByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据 ID 和账本 ID 查询分类
     *
     * @param id     分类 ID
     * @param bookId 账本 ID
     * @return 分类
     */
    @Select("SELECT * FROM categories WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Category selectByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);
}
