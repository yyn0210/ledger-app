package com.ledger.app.modules.category.service;

import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类列表（带父子层级）
     *
     * @param bookId 账本 ID
     * @param type   分类类型：1=支出 2=收入（null 表示全部）
     * @return 分类列表
     */
    List<CategoryResponse> getCategories(Long bookId, Integer type);

    /**
     * 获取分类详情
     *
     * @param id     分类 ID
     * @param bookId 账本 ID
     * @return 分类详情
     */
    CategoryResponse getCategory(Long id, Long bookId);

    /**
     * 创建分类
     *
     * @param request 创建请求
     * @return 分类 ID
     */
    Long createCategory(CreateCategoryRequest request);

    /**
     * 更新分类
     *
     * @param id      分类 ID
     * @param request 更新请求
     * @param bookId  账本 ID
     */
    void updateCategory(Long id, UpdateCategoryRequest request, Long bookId);

    /**
     * 删除分类
     *
     * @param id     分类 ID
     * @param bookId 账本 ID
     */
    void deleteCategory(Long id, Long bookId);

    /**
     * 获取系统预设分类
     *
     * @param type 分类类型：1=支出 2=收入（null 表示全部）
     * @return 系统预设分类列表
     */
    List<CategoryResponse> getSystemCategories(Integer type);

    /**
     * 初始化用户账本的系统预设分类
     *
     * @param bookId 账本 ID
     */
    void initSystemCategories(Long bookId);
}
