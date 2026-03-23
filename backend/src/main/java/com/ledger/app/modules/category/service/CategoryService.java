package com.ledger.app.modules.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.category.dto.CategoryResponse;
import com.ledger.app.modules.category.dto.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.UpdateCategoryRequest;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService extends ServiceImpl<CategoryRepository, Category> {

    private final CategoryRepository categoryRepository;

    /**
     * 获取分类列表（按类型分组，包含预置分类）
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<CategoryResponse>> getCategoriesByBookId(Long bookId) {
        // 查询账本自定义分类
        List<Category> customCategories = categoryRepository.findByBookId(bookId);
        
        // 查询预置分类
        List<Category> presetCategories = categoryRepository.findPresets();
        
        // 合并分类（预置分类 + 自定义分类）
        List<Category> allCategories = new ArrayList<>();
        allCategories.addAll(presetCategories);
        allCategories.addAll(customCategories);
        
        // 按类型分组
        return allCategories.stream()
                .collect(Collectors.groupingBy(
                        Category::getType,
                        Collectors.mapping(CategoryResponse::fromEntity, Collectors.toList())
                ));
    }

    /**
     * 获取分类详情
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId, Long bookId) {
        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return CategoryResponse.fromEntity(category);
    }

    /**
     * 获取预置分类模板
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<CategoryResponse>> getPresetCategories() {
        List<Category> presets = categoryRepository.findPresets();
        return presets.stream()
                .collect(Collectors.groupingBy(
                        Category::getType,
                        Collectors.mapping(CategoryResponse::fromEntity, Collectors.toList())
                ));
    }

    /**
     * 创建自定义分类
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        Category category = new Category();
        category.setBookId(request.getBookId());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setType(request.getType());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsPreset(0);
        category.setDeleted(0);

        categoryRepository.insert(category);
        log.info("创建分类成功：bookId={}, categoryId={}, name={}", 
                request.getBookId(), category.getId(), category.getName());

        return CategoryResponse.fromEntity(category);
    }

    /**
     * 更新分类（预置分类不可修改）
     */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, Long bookId, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 预置分类不可修改
        if (category.getIsPreset() == 1) {
            throw new BusinessException("预置分类不可修改");
        }

        // 更新字段
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        categoryRepository.updateById(category);
        log.info("更新分类成功：categoryId={}", categoryId);

        return CategoryResponse.fromEntity(category);
    }

    /**
     * 删除分类（预置分类不可删除）
     */
    @Transactional
    public void deleteCategory(Long categoryId, Long bookId) {
        Category category = categoryRepository.findByIdAndBookId(categoryId, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 预置分类不可删除
        if (category.getIsPreset() == 1) {
            throw new BusinessException("预置分类不可删除");
        }

        // 检查是否有子分类
        List<Category> children = categoryRepository.findChildrenByParentId(bookId, categoryId);
        if (!children.isEmpty()) {
            throw new BusinessException("请先删除子分类");
        }

        // 软删除
        category.setDeleted(1);
        categoryRepository.updateById(category);
        log.info("删除分类成功：categoryId={}", categoryId);
    }

    /**
     * 初始化账本分类（复制预置分类到用户账本）
     */
    @Transactional
    public void initBookCategories(Long bookId) {
        // 检查是否已初始化
        List<Category> existing = categoryRepository.findByBookId(bookId);
        if (!existing.isEmpty()) {
            return;
        }

        // 获取预置分类
        List<Category> presets = categoryRepository.findPresets();
        
        // 复制到用户账本
        for (Category preset : presets) {
            Category category = new Category();
            category.setBookId(bookId);
            category.setParentId(preset.getParentId());
            category.setName(preset.getName());
            category.setIcon(preset.getIcon());
            category.setType(preset.getType());
            category.setSortOrder(preset.getSortOrder());
            category.setIsPreset(0);
            category.setDeleted(0);
            categoryRepository.insert(category);
        }
        
        log.info("初始化账本分类成功：bookId={}, 分类数={}", bookId, presets.size());
    }
}
