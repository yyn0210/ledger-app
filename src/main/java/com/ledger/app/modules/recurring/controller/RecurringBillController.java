package com.ledger.app.modules.recurring.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.app.common.result.PageResult;
import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.recurring.dto.request.CreateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.request.UpdateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.response.RecurringBillResponse;
import com.ledger.app.modules.recurring.service.RecurringBillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 周期账单控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "周期账单管理", description = "周期账单 CRUD 及执行控制")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/recurring-bill")
@RequiredArgsConstructor
public class RecurringBillController {

    private final RecurringBillService recurringBillService;
    private final AuthService authService;

    /**
     * 获取当前用户 ID
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
     * 创建周期账单
     */
    @Operation(summary = "创建周期账单", description = "创建新的周期账单规则")
    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody CreateRecurringBillRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long id = recurringBillService.create(request, userId);
        return Result.success(id);
    }

    /**
     * 更新周期账单
     */
    @Operation(summary = "更新周期账单", description = "更新周期账单信息")
    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringBillRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.update(id, request, userId);
        return Result.success();
    }

    /**
     * 删除周期账单
     */
    @Operation(summary = "删除周期账单", description = "删除周期账单（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.delete(id, userId);
        return Result.success();
    }

    /**
     * 获取周期账单详情
     */
    @Operation(summary = "获取周期账单详情", description = "根据 ID 获取周期账单详细信息")
    @GetMapping("/{id}")
    public Result<RecurringBillResponse> getById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        RecurringBillResponse response = recurringBillService.getById(id, userId);
        return Result.success(response);
    }

    /**
     * 分页查询周期账单列表
     */
    @Operation(summary = "分页查询周期账单列表", description = "按账本分页查询周期账单")
    @GetMapping
    public Result<PageResult<RecurringBillResponse>> pageByBookId(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Page<RecurringBillResponse> responsePage = recurringBillService.pageByBookId(bookId, userId, page, size);

        PageResult<RecurringBillResponse> result = PageResult.<RecurringBillResponse>builder()
                .pageNum(responsePage.getCurrent())
                .pageSize(responsePage.getSize())
                .total(responsePage.getTotal())
                .pages(responsePage.getPages())
                .list(responsePage.getRecords())
                .hasPrevious(responsePage.getCurrent() > 1)
                .hasNext(responsePage.getCurrent() < responsePage.getPages())
                .build();

        return Result.success(result);
    }

    /**
     * 手动执行周期账单
     */
    @Operation(summary = "手动执行周期账单", description = "立即执行一次周期账单")
    @PostMapping("/{id}/execute")
    public Result<Void> execute(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.execute(id, userId);
        return Result.success();
    }

    /**
     * 暂停周期账单
     */
    @Operation(summary = "暂停周期账单", description = "暂停周期账单的执行")
    @PostMapping("/{id}/pause")
    public Result<Void> pause(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.pause(id, userId);
        return Result.success();
    }

    /**
     * 恢复周期账单
     */
    @Operation(summary = "恢复周期账单", description = "恢复已暂停的周期账单")
    @PostMapping("/{id}/resume")
    public Result<Void> resume(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.resume(id, userId);
        return Result.success();
    }

    /**
     * 跳过本次执行
     */
    @Operation(summary = "跳过本次执行", description = "跳过当前周期的执行，直接计算下次执行日期")
    @PostMapping("/{id}/skip")
    public Result<Void> skip(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        recurringBillService.skip(id, userId);
        return Result.success();
    }
}
