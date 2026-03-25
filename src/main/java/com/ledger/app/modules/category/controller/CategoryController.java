package com.ledger.app.modules.category.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;
import com.ledger.app.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "分类管理", description = "分类 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @Operation(summary = "获取分类列表", description = "获取指定账本的分类列表（支持按类型过滤，返回父子层级结构）")
    @GetMapping
    public Result<List<CategoryResponse>> getCategories(
            @RequestParam Long bookId,
            @RequestParam(required = false) Integer type) {
        List<CategoryResponse> categories = categoryService.getCategoriesByBookId(bookId, type);
        return Result.success(categories);
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情", description = "根据 ID 获取分类详细信息")
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategory(
            @PathVariable Long id,
            @RequestParam Long bookId) {
        CategoryResponse category = categoryService.getCategoryByIdAndBookId(id, bookId);
        return Result.success(category);
    }

    /**
     * 创建分类
     */
    @Operation(summary = "创建分类", description = "创建一个新的分类")
    @PostMapping
    public Result<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return Result.success(category);
    }

    /**
     * 更新分类
     */
    @Operation(summary = "更新分类", description = "更新指定分类的信息")
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @RequestParam Long bookId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, bookId, request);
        return Result.success(category);
    }

    /**
     * 删除分类
     */
    @Operation(summary = "删除分类", description = "软删除指定分类（系统预设分类和已被引用的分类不能删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @PathVariable Long id,
            @RequestParam Long bookId) {
        categoryService.deleteCategory(id, bookId);
        return Result.success(null);
    }

    /**
     * 获取系统预设分类
     */
    @Operation(summary = "获取系统预设分类", description = "获取系统预设的分类模板（支出 10 个 + 收入 5 个）")
    @GetMapping("/system")
    public Result<List<CategoryResponse>> getSystemCategories(
            @RequestParam(required = false) Integer type) {
        List<CategoryResponse> categories = categoryService.getSystemCategories(type);
        return Result.success(categories);
    }
}
