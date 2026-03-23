package com.ledger.app.modules.category.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.book.service.BookService;
import com.ledger.app.modules.category.dto.CategoryResponse;
import com.ledger.app.modules.category.dto.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.UpdateCategoryRequest;
import com.ledger.app.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "分类 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;
    private final BookService bookService;

    /**
     * 获取当前用户 ID（从 JWT Token 解析）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        return authService.getUserIdFromToken(token);
    }

    /**
     * 从请求头获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取分类列表（按类型分组）
     */
    @Operation(summary = "获取分类列表", description = "获取指定账本的分类列表（包含预置分类），按支出/收入分组")
    @GetMapping
    public Result<Map<Integer, List<CategoryResponse>>> getCategories(
            @RequestParam(required = false) Long bookId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        
        // 如果没有传入 bookId，使用默认账本
        if (bookId == null) {
            var defaultBook = bookService.getOrCreateDefaultBook(userId);
            bookId = defaultBook.getId();
        }
        
        Map<Integer, List<CategoryResponse>> categories = categoryService.getCategoriesByBookId(bookId);
        return Result.success(categories);
    }

    /**
     * 获取分类详情
     */
    @Operation(summary = "获取分类详情", description = "根据 ID 获取分类详细信息")
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategory(@PathVariable Long id,
                                                 @RequestParam Long bookId,
                                                 HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        CategoryResponse category = categoryService.getCategoryById(id, bookId);
        return Result.success(category);
    }

    /**
     * 获取预置分类模板
     */
    @Operation(summary = "获取预置分类模板", description = "获取系统预置的分类模板（餐饮、交通、购物等）")
    @GetMapping("/presets")
    public Result<Map<Integer, List<CategoryResponse>>> getPresetCategories() {
        Map<Integer, List<CategoryResponse>> presets = categoryService.getPresetCategories();
        return Result.success(presets);
    }

    /**
     * 创建自定义分类
     */
    @Operation(summary = "创建自定义分类", description = "为指定账本创建新的自定义分类")
    @PostMapping
    public Result<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        CategoryResponse category = categoryService.createCategory(request);
        return Result.success(category);
    }

    /**
     * 更新分类
     */
    @Operation(summary = "更新分类", description = "更新指定分类的信息（预置分类不可修改）")
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(@PathVariable Long id,
                                                    @RequestParam Long bookId,
                                                    @Valid @RequestBody UpdateCategoryRequest request,
                                                    HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        CategoryResponse category = categoryService.updateCategory(id, bookId, request);
        return Result.success(category);
    }

    /**
     * 删除分类
     */
    @Operation(summary = "删除分类", description = "删除指定分类（预置分类不可删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id,
                                        @RequestParam Long bookId,
                                        HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        categoryService.deleteCategory(id, bookId);
        return Result.success(null);
    }

    /**
     * 初始化账本分类
     */
    @Operation(summary = "初始化账本分类", description = "为指定账本复制预置分类模板")
    @PostMapping("/init")
    public Result<Void> initBookCategories(@RequestParam Long bookId,
                                            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        categoryService.initBookCategories(bookId);
        return Result.success(null);
    }
}
