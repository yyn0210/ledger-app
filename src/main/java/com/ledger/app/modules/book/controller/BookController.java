package com.ledger.app.modules.book.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.book.dto.BookResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.modules.book.dto.UpdateBookRequest;
<<<<<<< HEAD
import com.ledger.app.modules.book.dto.request.AddMemberRequest;
import com.ledger.app.modules.book.entity.BookMember;
=======
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
import com.ledger.app.modules.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账本控制器
 */
@Tag(name = "账本管理", description = "账本 CRUD 操作接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final AuthService authService;

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
     * 获取我的账本列表
     */
    @Operation(summary = "获取我的账本列表", description = "返回当前用户的所有账本（按排序顺序）")
    @GetMapping
    public Result<List<BookResponse>> getMyBooks(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        List<BookResponse> books = bookService.getBooksByUserId(userId);
        return Result.success(books);
    }

    /**
     * 获取账本详情
     */
    @Operation(summary = "获取账本详情", description = "根据 ID 获取账本详细信息")
    @GetMapping("/{id}")
    public Result<BookResponse> getBook(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        BookResponse book = bookService.getBookByIdAndUserId(id, userId);
        return Result.success(book);
    }

    /**
     * 创建新账本
     */
    @Operation(summary = "创建新账本", description = "创建一个新的账本")
    @PostMapping
    public Result<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request,
                                           HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        BookResponse book = bookService.createBook(userId, request);
        return Result.success(book);
    }

    /**
     * 更新账本
     */
    @Operation(summary = "更新账本", description = "更新指定账本的信息")
    @PutMapping("/{id}")
    public Result<BookResponse> updateBook(@PathVariable Long id,
                                           @Valid @RequestBody UpdateBookRequest request,
                                           HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        BookResponse book = bookService.updateBook(id, userId, request);
        return Result.success(book);
    }

    /**
     * 删除账本（软删除）
     */
    @Operation(summary = "删除账本", description = "软删除指定账本（默认账本不能删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteBook(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        bookService.deleteBook(id, userId);
        return Result.success(null);
    }

    /**
     * 获取或创建默认账本
     */
    @Operation(summary = "获取或创建默认账本", description = "获取用户的默认账本，如果没有则创建一个")
    @GetMapping("/default")
    public Result<BookResponse> getOrCreateDefaultBook(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        BookResponse book = bookService.getOrCreateDefaultBook(userId);
        return Result.success(book);
    }
<<<<<<< HEAD

    // ==================== 成员管理接口 ====================

    /**
     * 添加成员到账本
     */
    @Operation(summary = "添加成员", description = "添加用户到账本（需要 owner 或 admin 权限）")
    @PostMapping("/{bookId}/members")
    public Result<BookMember> addMember(@PathVariable Long bookId,
                                        @Valid @RequestBody AddMemberRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        // TODO: 根据手机号查询用户 ID
        BookMember member = bookService.addMember(bookId, userId, userId, request.getRole());
        return Result.success(member);
    }

    /**
     * 移除账本成员
     */
    @Operation(summary = "移除成员", description = "从账本中移除成员（需要 owner 或 admin 权限）")
    @DeleteMapping("/{bookId}/members/{targetUserId}")
    public Result<Void> removeMember(@PathVariable Long bookId,
                                     @PathVariable Long targetUserId,
                                     HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        bookService.removeMember(bookId, userId, targetUserId);
        return Result.success(null);
    }

    /**
     * 更新成员角色
     */
    @Operation(summary = "更新成员角色", description = "修改成员的角色（只有 owner 可以操作）")
    @PutMapping("/{bookId}/members/{targetUserId}/role")
    public Result<Void> updateMemberRole(@PathVariable Long bookId,
                                         @PathVariable Long targetUserId,
                                         @RequestParam Integer role,
                                         HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        bookService.updateMemberRole(bookId, userId, targetUserId, role);
        return Result.success(null);
    }

    /**
     * 获取账本成员列表
     */
    @Operation(summary = "获取成员列表", description = "获取账本的所有成员")
    @GetMapping("/{bookId}/members")
    public Result<List<BookMember>> getMembers(@PathVariable Long bookId,
                                               HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未认证");
        }
        List<BookMember> members = bookService.getMembersByBookId(bookId, userId);
        return Result.success(members);
    }
=======
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
}
