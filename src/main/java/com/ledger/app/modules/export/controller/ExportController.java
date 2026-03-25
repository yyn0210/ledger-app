package com.ledger.app.modules.export.controller;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.auth.service.AuthService;
import com.ledger.app.modules.export.dto.request.ExportRequest;
import com.ledger.app.modules.export.dto.response.ExportResponse;
import com.ledger.app.modules.export.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据导出控制器
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Tag(name = "数据导出管理", description = "数据导出 CRUD 及文件下载接口")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
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
     * 创建导出任务
     */
    @Operation(summary = "创建导出任务", description = "创建新的导出任务（支持交易/预算/统计）")
    @PostMapping
    public Result<ExportResponse> createExportTask(
            @Valid @RequestBody ExportRequest request,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ExportResponse response = exportService.createExportTask(userId, request);
        return Result.success(response);
    }

    /**
     * 获取导出记录详情
     */
    @Operation(summary = "获取导出记录详情", description = "根据 ID 获取导出记录详细信息")
    @GetMapping("/{id}")
    public Result<ExportResponse> getExportRecord(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ExportResponse response = exportService.getExportRecord(id, userId);
        return Result.success(response);
    }

    /**
     * 获取导出记录列表
     */
    @Operation(summary = "获取导出记录列表", description = "获取用户的导出记录列表")
    @GetMapping
    public Result<List<ExportResponse>> getExportRecords(
            @RequestParam(required = false) Long bookId,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        List<ExportResponse> records = exportService.getExportRecords(userId, bookId, limit);
        return Result.success(records);
    }

    /**
     * 下载导出文件
     */
    @Operation(summary = "下载导出文件", description = "下载已生成的导出文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Resource resource = exportService.downloadFile(id, userId);

        ExportResponse record = exportService.getExportRecord(id, userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + record.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 删除导出记录
     */
    @Operation(summary = "删除导出记录", description = "软删除指定导出记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteExportRecord(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        exportService.deleteExportRecord(id, userId);
        return Result.success();
    }

    /**
     * 导出交易记录为 Excel
     */
    @Operation(summary = "导出交易记录为 Excel", description = "导出交易记录为 Excel 文件")
    @PostMapping("/transactions/excel")
    public Result<Long> exportTransactionsToExcel(
            @RequestParam Long bookId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportTransactionsToExcel(bookId, userId, startDate, endDate, categoryId);
        return Result.success(recordId);
    }

    /**
     * 导出交易记录为 CSV
     */
    @Operation(summary = "导出交易记录为 CSV", description = "导出交易记录为 CSV 文件")
    @PostMapping("/transactions/csv")
    public Result<Long> exportTransactionsToCsv(
            @RequestParam Long bookId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportTransactionsToCsv(bookId, userId, startDate, endDate, categoryId);
        return Result.success(recordId);
    }

    /**
     * 导出预算报表为 Excel
     */
    @Operation(summary = "导出预算报表为 Excel", description = "导出预算报表为 Excel 文件")
    @PostMapping("/budget/excel")
    public Result<Long> exportBudgetToExcel(
            @RequestParam Long bookId,
            @RequestParam(required = false) String period,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportBudgetToExcel(bookId, userId, period);
        return Result.success(recordId);
    }

    /**
     * 导出预算报表为 CSV
     */
    @Operation(summary = "导出预算报表为 CSV", description = "导出预算报表为 CSV 文件")
    @PostMapping("/budget/csv")
    public Result<Long> exportBudgetToCsv(
            @RequestParam Long bookId,
            @RequestParam(required = false) String period,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportBudgetToCsv(bookId, userId, period);
        return Result.success(recordId);
    }

    /**
     * 导出预算报表为 PDF
     */
    @Operation(summary = "导出预算报表为 PDF", description = "导出预算报表为 PDF 文件")
    @PostMapping("/budget/pdf")
    public Result<Long> exportBudgetToPdf(
            @RequestParam Long bookId,
            @RequestParam(required = false) String period,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportBudgetToPdf(bookId, userId, period);
        return Result.success(recordId);
    }

    /**
     * 导出统计报表为 Excel
     */
    @Operation(summary = "导出统计报表为 Excel", description = "导出统计报表为 Excel 文件")
    @PostMapping("/statistics/excel")
    public Result<Long> exportStatisticsToExcel(
            @RequestParam Long bookId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportStatisticsToExcel(bookId, userId, startDate, endDate);
        return Result.success(recordId);
    }

    /**
     * 导出统计报表为 CSV
     */
    @Operation(summary = "导出统计报表为 CSV", description = "导出统计报表为 CSV 文件")
    @PostMapping("/statistics/csv")
    public Result<Long> exportStatisticsToCsv(
            @RequestParam Long bookId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportStatisticsToCsv(bookId, userId, startDate, endDate);
        return Result.success(recordId);
    }

    /**
     * 导出统计报表为 PDF
     */
    @Operation(summary = "导出统计报表为 PDF", description = "导出统计报表为 PDF 文件")
    @PostMapping("/statistics/pdf")
    public Result<Long> exportStatisticsToPdf(
            @RequestParam Long bookId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Long recordId = exportService.exportStatisticsToPdf(bookId, userId, startDate, endDate);
        return Result.success(recordId);
    }
}
