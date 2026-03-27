package com.ledger.app.modules.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByBookId(Long bookId, Integer type) {
        // 获取一级分类
        List<Category> parentCategories = categoryRepository.findParentCategoriesByBookId(bookId, type);

        // 组装父子层级结构
        return parentCategories.stream()
                .map(parent -> {
                    CategoryResponse parentResponse = CategoryResponse.fromEntity(parent);
                    // 获取子分类
                    List<Category> children = categoryRepository.findChildrenByParentId(parent.getId());
                    List<CategoryResponse> childrenResponses = children.stream()
                            .map(CategoryResponse::fromEntity)
                            .collect(Collectors.toList());
                    parentResponse.setChildren(childrenResponses);
                    return parentResponse;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByIdAndBookId(Long id, Long bookId) {
        Category category = categoryRepository.findByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在或无权访问");
        }
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        // 检查分类名称是否重复
        int count = categoryRepository.countByName(request.getBookId(), request.getName(), 0L);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        // 创建分类
        Category category = new Category();
        category.setBookId(request.getBookId());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setType(request.getType());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsSystem(0); // 用户自定义分类
        category.setDeleted(0);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.insert(category);
        log.info("创建分类成功：bookId={}, categoryId={}, name={}", request.getBookId(), category.getId(), category.getName());

        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, Long bookId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在或无权访问");
        }

        // 系统预设分类不能修改
        if (category.getIsSystem() == 1) {
            throw new BusinessException("系统预设分类不能修改");
        }

        // 检查分类名称是否重复（排除自己）
        if (request.getName() != null) {
            int count = categoryRepository.countByName(bookId, request.getName(), id);
            if (count > 0) {
                throw new BusinessException("分类名称已存在");
            }
            category.setName(request.getName());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }

        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.updateById(category);
        log.info("更新分类成功：bookId={}, categoryId={}", bookId, id);

        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, Long bookId) {
        Category category = categoryRepository.findByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在或无权访问");
        }

        // 系统预设分类不能删除
        if (category.getIsSystem() == 1) {
            throw new BusinessException("系统预设分类不能删除");
        }

        // TODO: 检查是否有交易记录使用此分类（依赖交易模块）
        // Long count = transactionRepository.countByCategoryId(id);
        // if (count > 0) {
        //     throw new BusinessException("该分类已被交易记录使用，无法删除");
        // }

        // 检查是否有子分类
        List<Category> children = categoryRepository.findChildrenByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下存在子分类，请先删除子分类");
        }

        // 软删除
        category.setDeleted(1);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.updateById(category);
        log.info("删除分类成功：bookId={}, categoryId={}", bookId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSystemCategories(Integer type) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getIsSystem, 1)
                .eq(Category::getDeleted, 0);

        if (type != null) {
            queryWrapper.eq(Category::getType, type);
        }

        queryWrapper.orderByAsc(Category::getSortOrder);

        List<Category> categories = categoryRepository.selectList(queryWrapper);
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
