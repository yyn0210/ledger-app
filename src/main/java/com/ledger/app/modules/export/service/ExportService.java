package com.ledger.app.modules.export.service;

import com.ledger.app.modules.export.dto.request.ExportRequest;
import com.ledger.app.modules.export.dto.response.ExportResponse;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * 导出服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface ExportService {

    /**
     * 创建导出任务
     *
     * @param userId 用户 ID
     * @param request 导出请求
     * @return 导出记录
     */
    ExportResponse createExportTask(Long userId, ExportRequest request);

    /**
     * 获取导出记录详情
     *
     * @param id 导出记录 ID
     * @param userId 用户 ID
     * @return 导出记录
     */
    ExportResponse getExportRecord(Long id, Long userId);

    /**
     * 获取导出记录列表
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID（可选）
     * @param limit 数量限制
     * @return 导出记录列表
     */
    List<ExportResponse> getExportRecords(Long userId, Long bookId, Integer limit);

    /**
     * 下载导出文件
     *
     * @param id 导出记录 ID
     * @param userId 用户 ID
     * @return 文件资源
     */
    Resource downloadFile(Long id, Long userId);

    /**
     * 删除导出记录
     *
     * @param id 导出记录 ID
     * @param userId 用户 ID
     */
    void deleteExportRecord(Long id, Long userId);

    /**
     * 导出交易记录为 Excel
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param categoryId 分类 ID（可选）
     * @return 导出记录 ID
     */
    Long exportTransactionsToExcel(Long bookId, Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate, Long categoryId);

    /**
     * 导出交易记录为 CSV
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param categoryId 分类 ID（可选）
     * @return 导出记录 ID
     */
    Long exportTransactionsToCsv(Long bookId, Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate, Long categoryId);

    /**
     * 导出预算报表为 Excel
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期
     * @return 导出记录 ID
     */
    Long exportBudgetToExcel(Long bookId, Long userId, String period);

    /**
     * 导出预算报表为 CSV
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期
     * @return 导出记录 ID
     */
    Long exportBudgetToCsv(Long bookId, Long userId, String period);

    /**
     * 导出预算报表为 PDF
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param period 周期
     * @return 导出记录 ID
     */
    Long exportBudgetToPdf(Long bookId, Long userId, String period);

    /**
     * 导出统计报表为 Excel
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出记录 ID
     */
    Long exportStatisticsToExcel(Long bookId, Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 导出统计报表为 CSV
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出记录 ID
     */
    Long exportStatisticsToCsv(Long bookId, Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 导出统计报表为 PDF
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出记录 ID
     */
    Long exportStatisticsToPdf(Long bookId, Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
