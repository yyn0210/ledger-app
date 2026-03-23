package com.ledger.app.modules.category.repository;

import com.ledger.app.modules.category.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类数据访问层
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {

    /**
     * 查询账本的所有分类（按类型和排序）
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND deleted = 0 ORDER BY type, sort_order, created_at DESC")
    List<Category> findByBookId(@Param("bookId") Long bookId);

    /**
     * 查询预置分类（全局可用）
     */
    @Select("SELECT * FROM categories WHERE book_id = 0 AND is_preset = 1 AND deleted = 0 ORDER BY type, sort_order")
    List<Category> findPresets();

    /**
     * 根据 ID 和账本 ID 查询分类（权限检查）
     */
    @Select("SELECT * FROM categories WHERE id = #{id} AND (book_id = #{bookId} OR book_id = 0) AND deleted = 0 LIMIT 1")
    Category findByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 查询一级分类
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND parent_id = 0 AND deleted = 0 ORDER BY sort_order")
    List<Category> findTopLevelByBookId(@Param("bookId") Long bookId);

    /**
     * 查询子分类
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Category> findChildrenByParentId(@Param("bookId") Long bookId, @Param("parentId") Long parentId);
}
