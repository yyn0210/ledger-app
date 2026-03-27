package com.ledger.app.modules.category.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.enums.CategoryType;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.category.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getCategories(Long bookId, Integer type) {
        log.info("获取分类列表，bookId: {}, type: {}", bookId, type);

        List<Category> categories;
        if (type != null) {
            categories = categoryRepository.selectByBookIdAndType(bookId, type);
        } else {
            categories = categoryRepository.selectByBookId(bookId);
        }

        // 构建父子层级结构
        return buildCategoryTree(categories);
    }

    @Override
    public CategoryResponse getCategory(Long id, Long bookId) {
        log.info("获取分类详情，id: {}, bookId: {}", id, bookId);

        Category category = categoryRepository.selectByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        return convertToResponse(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CreateCategoryRequest request) {
        log.info("创建分类，request: {}", request);

        // 验证分类类型
        if (request.getType() != null) {
            CategoryType.fromCode(request.getType());
        }

        // 检查父分类是否存在（如果 parentId > 0）
        if (request.getParentId() != null && request.getParentId() > 0) {
            Category parent = categoryRepository.selectByIdAndBookId(
                    request.getParentId(), request.getBookId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
        }

        // 创建分类
        Category category = new Category();
        category.setBookId(request.getBookId());
        category.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setType(request.getType());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsSystem(0); // 用户创建的分类不是系统预设
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.insert(category);

        log.info("分类创建成功，id: {}", category.getId());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, UpdateCategoryRequest request, Long bookId) {
        log.info("更新分类，id: {}, request: {}", id, request);

        Category category = categoryRepository.selectByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 系统预设分类不能修改
        if (category.getIsSystem() != null && category.getIsSystem() == 1) {
            throw new BusinessException("系统预设分类不能修改");
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder());
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.updateById(category);

        log.info("分类更新成功，id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id, Long bookId) {
        log.info("删除分类，id: {}, bookId: {}", id, bookId);

        Category category = categoryRepository.selectByIdAndBookId(id, bookId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 系统预设分类不能删除
        if (category.getIsSystem() != null && category.getIsSystem() == 1) {
            throw new BusinessException("系统预设分类不能删除");
        }

        // 检查是否有子分类
        List<Category> children = categoryRepository.selectByBookIdAndType(bookId, category.getType())
                .stream()
                .filter(c -> c.getParentId().equals(id))
                .collect(Collectors.toList());
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下有子分类，请先删除子分类");
        }

        // 检查是否被交易记录引用
        Long count = categoryRepository.countTransactionsByCategoryId(id);
        if (count > 0) {
            throw new BusinessException("该分类已被交易记录使用，无法删除");
        }

        // 软删除
        categoryRepository.deleteById(id);

        log.info("分类删除成功，id: {}", id);
    }

    @Override
    public List<CategoryResponse> getSystemCategories(Integer type) {
        log.info("获取系统预设分类，type: {}", type);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsSystem, 1);
        if (type != null) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.orderByAsc(Category::getType, Category::getSortOrder);

        List<Category> categories = categoryRepository.selectList(wrapper);
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSystemCategories(Long bookId) {
        log.info("初始化系统预设分类，bookId: {}", bookId);

        // 检查是否已初始化
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getBookId, bookId)
                .eq(Category::getIsSystem, 1);
        Long count = categoryRepository.selectCount(wrapper);
        if (count > 0) {
            log.info("账本 {} 已初始化系统预设分类", bookId);
            return;
        }

        // 支出分类
        List<Category> expenseCategories = getExpensePresetCategories(bookId);
        categoryRepository.insertBatch(expenseCategories);

        // 收入分类
        List<Category> incomeCategories = getIncomePresetCategories(bookId);
        categoryRepository.insertBatch(incomeCategories);

        log.info("系统预设分类初始化完成，bookId: {}", bookId);
    }

    /**
     * 获取支出预设分类
     */
    private List<Category> getExpensePresetCategories(Long bookId) {
        List<Category> categories = new ArrayList<>();
        categories.add(createPresetCategory(bookId, 0L, "餐饮", "🍜", 1, 1));
        categories.add(createPresetCategory(bookId, 0L, "交通", "🚗", 1, 2));
        categories.add(createPresetCategory(bookId, 0L, "购物", "🛍️", 1, 3));
        categories.add(createPresetCategory(bookId, 0L, "娱乐", "🎬", 1, 4));
        categories.add(createPresetCategory(bookId, 0L, "居住", "🏠", 1, 5));
        categories.add(createPresetCategory(bookId, 0L, "通讯", "📱", 1, 6));
        categories.add(createPresetCategory(bookId, 0L, "医疗", "🏥", 1, 7));
        categories.add(createPresetCategory(bookId, 0L, "教育", "📚", 1, 8));
        categories.add(createPresetCategory(bookId, 0L, "人情", "🧧", 1, 9));
        categories.add(createPresetCategory(bookId, 0L, "其他", "📦", 1, 10));
        return categories;
    }

    /**
     * 获取收入预设分类
     */
    private List<Category> getIncomePresetCategories(Long bookId) {
        List<Category> categories = new ArrayList<>();
        categories.add(createPresetCategory(bookId, 0L, "工资", "💰", 2, 1));
        categories.add(createPresetCategory(bookId, 0L, "奖金", "🎁", 2, 2));
        categories.add(createPresetCategory(bookId, 0L, "理财", "📈", 2, 3));
        categories.add(createPresetCategory(bookId, 0L, "兼职", "💼", 2, 4));
        categories.add(createPresetCategory(bookId, 0L, "其他", "📦", 2, 5));
        return categories;
    }

    /**
     * 创建预设分类
     */
    private Category createPresetCategory(Long bookId, Long parentId, String name,
                                           String icon, Integer type, Integer sortOrder) {
        Category category = new Category();
        category.setBookId(bookId);
        category.setParentId(parentId);
        category.setName(name);
        category.setIcon(icon);
        category.setType(type);
        category.setSortOrder(sortOrder);
        category.setIsSystem(1); // 系统预设
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setDeleted(0);
        return category;
    }

    /**
     * 构建分类树形结构
     */
    private List<CategoryResponse> buildCategoryTree(List<Category> categories) {
        // 一级分类
        List<CategoryResponse> rootCategories = categories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // 为每个一级分类添加子分类
        for (CategoryResponse root : rootCategories) {
            List<CategoryResponse> children = categories.stream()
                    .filter(c -> root.getId().equals(c.getParentId()))
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            root.setChildren(children);
        }

        return rootCategories;
    }

    /**
     * 转换 Entity 到 Response
     */
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setBookId(category.getBookId());
        response.setParentId(category.getParentId());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setType(category.getType());
        response.setSortOrder(category.getSortOrder());
        response.setIsSystem(category.getIsSystem());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
