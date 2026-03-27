package com.ledger.app.modules.category.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.app.modules.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {

    /**
     * 根据账本 ID 获取分类列表（按排序顺序）
     *
     * @param bookId 账本 ID
     * @param type 分类类型（1=支出 2=收入，null 表示全部）
     * @return 分类列表
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND deleted = 0 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "ORDER BY sort_order ASC")
    List<Category> findByBookId(@Param("bookId") Long bookId, @Param("type") Integer type);

    /**
     * 根据账本 ID 获取一级分类
     *
     * @param bookId 账本 ID
     * @param type 分类类型
     * @return 一级分类列表
     */
    @Select("SELECT * FROM categories WHERE book_id = #{bookId} AND parent_id = 0 AND deleted = 0 " +
            "<if test='type != null'>AND type = #{type}</if> " +
            "ORDER BY sort_order ASC")
    List<Category> findParentCategoriesByBookId(@Param("bookId") Long bookId, @Param("type") Integer type);

    /**
     * 根据父分类 ID 获取子分类
     *
     * @param parentId 父分类 ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM categories WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 根据 ID 和账本 ID 获取分类（权限检查）
     *
     * @param id 分类 ID
     * @param bookId 账本 ID
     * @return 分类信息
     */
    @Select("SELECT * FROM categories WHERE id = #{id} AND book_id = #{bookId} AND deleted = 0")
    Category findByIdAndBookId(@Param("id") Long id, @Param("bookId") Long bookId);

    /**
     * 统计系统预设分类数量
     *
     * @param bookId 账本 ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM categories WHERE book_id = #{bookId} AND is_system = 1 AND deleted = 0")
    int countSystemCategories(@Param("bookId") Long bookId);

    /**
     * 检查分类名称是否重复
     *
     * @param bookId 账本 ID
     * @param name 分类名称
     * @param excludeId 排除的分类 ID（更新时使用）
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM categories WHERE book_id = #{bookId} AND name = #{name} AND id != #{excludeId} AND deleted = 0")
    int countByName(@Param("bookId") Long bookId, @Param("name") String name, @Param("excludeId") Long excludeId);
}
