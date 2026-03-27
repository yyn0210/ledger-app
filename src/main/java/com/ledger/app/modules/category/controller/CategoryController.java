package com.ledger.app.modules.category.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;
import com.ledger.app.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 分类管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表
     * GET /api/categories?bookId={bookId}&type={type}
     */
    @GetMapping
    public Result<List<CategoryResponse>> getCategories(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId,
            @RequestParam(required = false) Integer type) {
        log.info("获取分类列表，bookId: {}, type: {}", bookId, type);
        List<CategoryResponse> categories = categoryService.getCategories(bookId, type);
        return Result.success(categories);
    }

    /**
     * 获取分类详情
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategory(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId) {
        log.info("获取分类详情，id: {}, bookId: {}", id, bookId);
        CategoryResponse category = categoryService.getCategory(id, bookId);
        return Result.success(category);
    }

    /**
     * 创建分类
     * POST /api/categories
     */
    @PostMapping
    public Result<Long> createCategory(@RequestBody @Validated CreateCategoryRequest request) {
        log.info("创建分类，request: {}", request);
        Long categoryId = categoryService.createCategory(request);
        return Result.success(categoryId);
    }

    /**
     * 更新分类
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable Long id,
            @RequestBody @Validated UpdateCategoryRequest request,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId) {
        log.info("更新分类，id: {}, bookId: {}", id, bookId);
        categoryService.updateCategory(id, request, bookId);
        return Result.success(null);
    }

    /**
     * 删除分类
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId) {
        log.info("删除分类，id: {}, bookId: {}", id, bookId);
        categoryService.deleteCategory(id, bookId);
        return Result.success(null);
    }

    /**
     * 获取系统预设分类
     * GET /api/categories/system?type={type}
     */
    @GetMapping("/system")
    public Result<List<CategoryResponse>> getSystemCategories(
            @RequestParam(required = false) Integer type) {
        log.info("获取系统预设分类，type: {}", type);
        List<CategoryResponse> categories = categoryService.getSystemCategories(type);
        return Result.success(categories);
    }

    /**
     * 初始化系统预设分类
     * POST /api/categories/system/init?bookId={bookId}
     */
    @PostMapping("/system/init")
    public Result<Void> initSystemCategories(
            @RequestParam @NotNull(message = "账本 ID 不能为空") Long bookId) {
        log.info("初始化系统预设分类，bookId: {}", bookId);
        categoryService.initSystemCategories(bookId);
        return Result.success(null);
    }
}
