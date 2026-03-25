package com.ledger.app.modules.category;

import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.enums.CategoryType;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.category.service.impl.CategoryServiceImpl;
import com.ledger.app.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 分类服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("餐饮")
                .type(CategoryType.EXPENSE.getCode())
                .icon("food")
                .userId(1L)
                .isSystem(false)
                .build();
    }

    @Test
    void testCreateCategory() {
        // 准备
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // 执行
        Long categoryId = categoryService.create(category, 1L);

        // 验证
        assertNotNull(categoryId);
        assertEquals(1L, categoryId);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testGetCategoriesByType() {
        // 准备
        List<Category> categories = Arrays.asList(category);
        when(categoryRepository.findByUserIdAndTypeAndIsSystem(1L, CategoryType.EXPENSE.getCode(), 0))
                .thenReturn(categories);

        // 执行
        List<Category> result = categoryService.getCategoriesByType(1L, CategoryType.EXPENSE.getCode());

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("餐饮", result.get(0).getName());
    }

    @Test
    void testUpdateCategory() {
        // 准备
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // 执行
        category.setName("更新后的餐饮");
        categoryService.update(1L, category, 1L);

        // 验证
        verify(categoryRepository, times(1)).save(category);
        assertEquals("更新后的餐饮", category.getName());
    }

    @Test
    void testUpdateSystemCategory_ThrowsException() {
        // 准备
        Category systemCategory = Category.builder()
                .id(1L)
                .name("系统分类")
                .isSystem(true)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(systemCategory));

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> categoryService.update(1L, systemCategory, 1L));
    }

    @Test
    void testDeleteCategory() {
        // 准备
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // 执行
        categoryService.delete(1L, 1L);

        // 验证
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteSystemCategory_ThrowsException() {
        // 准备
        Category systemCategory = Category.builder()
                .id(1L)
                .name("系统分类")
                .isSystem(true)
                .build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(systemCategory));

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> categoryService.delete(1L, 1L));
    }
}
