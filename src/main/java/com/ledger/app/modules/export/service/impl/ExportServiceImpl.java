package com.ledger.app.modules.export.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.export.dto.request.ExportRequest;
import com.ledger.app.modules.export.dto.response.ExportResponse;
import com.ledger.app.modules.export.entity.ExportRecord;
import com.ledger.app.modules.export.enums.ExportStatus;
import com.ledger.app.modules.export.repository.ExportRecordRepository;
import com.ledger.app.modules.export.service.ExportService;
import com.ledger.app.modules.export.utils.CsvExportUtil;
import com.ledger.app.modules.export.utils.ExcelExportUtil;
import com.ledger.app.modules.export.utils.PdfExportUtil;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 导出服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExportRecordRepository exportRecordRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    @Value("${minio.bucket-name:ledger-app}")
    private String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String minioEndpoint;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @Override
    @Transactional
    public ExportResponse createExportTask(Long userId, ExportRequest request) {
        // 创建导出记录
        ExportRecord record = ExportRecord.builder()
                .userId(userId)
                .bookId(request.getBookId())
                .exportType(request.getType())
                .fileFormat(request.getFormat())
                .fileName(generateFileName(request.getType(), request.getFormat()))
                .status(ExportStatus.PENDING.getCode())
                .expireAt(LocalDateTime.now().plusDays(7)) // 7 天后过期
                .deleted(0)
                .build();

        exportRecordRepository.insert(record);
        log.info("创建导出任务：recordId={}, type={}, format={}", record.getId(), request.getType(), request.getFormat());

        // 异步执行导出
        executeExportAsync(record.getId(), userId, request);

        return buildResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public ExportResponse getExportRecord(Long id, Long userId) {
        ExportRecord record = getExportRecordEntity(id, userId);
        return buildResponse(record);
    }

    /**
     * 获取导出记录实体
     */
    private ExportRecord getExportRecordEntity(Long id, Long userId) {
        ExportRecord record = exportRecordRepository.selectById(id);
        if (record == null || record.getDeleted() != 0) {
            throw new BusinessException("导出记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该导出记录");
        }
        return record;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExportResponse> getExportRecords(Long userId, Long bookId, Integer limit) {
        List<ExportRecord> records;
        if (bookId != null) {
            records = exportRecordRepository.findByUserIdAndBookId(userId, bookId, limit != null ? limit : 20);
        } else {
            records = exportRecordRepository.findByUserId(userId, limit != null ? limit : 20);
        }
        return records.stream().map(this::buildResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long id, Long userId) {
        ExportRecord record = getExportRecordEntity(id, userId);
        if (record.getStatus() != ExportStatus.COMPLETED.getCode()) {
            throw new BusinessException("文件尚未生成完成");
        }
        // 如果文件路径存在且以 data: 开头，说明是内联数据
        if (record.getFilePath() != null && record.getFilePath().startsWith("data:")) {
            String base64Data = record.getFilePath().substring(record.getFilePath().indexOf(",") + 1);
            byte[] fileData = Base64.getDecoder().decode(base64Data);
            return new ByteArrayResource(fileData);
        }
        // TODO: 从 MinIO 下载文件（待集成）
        throw new BusinessException("文件存储尚未配置，请联系管理员");
    }

    @Override
    @Transactional
    public void deleteExportRecord(Long id, Long userId) {
        getExportRecordEntity(id, userId);
        exportRecordRepository.deleteById(id);
        log.info("删除导出记录：recordId={}", id);
    }

    @Override
    @Transactional
    public Long exportTransactionsToExcel(Long bookId, Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        return createExportRecordAndExecute(bookId, userId, "transaction", "excel", startDate, endDate, categoryId);
    }

    @Override
    @Transactional
    public Long exportTransactionsToCsv(Long bookId, Long userId, LocalDate startDate, LocalDate endDate, Long categoryId) {
        return createExportRecordAndExecute(bookId, userId, "transaction", "csv", startDate, endDate, categoryId);
    }

    @Override
    @Transactional
    public Long exportBudgetToExcel(Long bookId, Long userId, String period) {
        return createExportRecordAndExecute(bookId, userId, "budget", "excel", null, null, null, period);
    }

    @Override
    @Transactional
    public Long exportBudgetToCsv(Long bookId, Long userId, String period) {
        return createExportRecordAndExecute(bookId, userId, "budget", "csv", null, null, null, period);
    }

    @Override
    @Transactional
    public Long exportBudgetToPdf(Long bookId, Long userId, String period) {
        return createExportRecordAndExecute(bookId, userId, "budget", "pdf", null, null, null, period);
    }

    @Override
    @Transactional
    public Long exportStatisticsToExcel(Long bookId, Long userId, LocalDate startDate, LocalDate endDate) {
        return createExportRecordAndExecute(bookId, userId, "statistics", "excel", startDate, endDate, null, null);
    }

    @Override
    @Transactional
    public Long exportStatisticsToCsv(Long bookId, Long userId, LocalDate startDate, LocalDate endDate) {
        return createExportRecordAndExecute(bookId, userId, "statistics", "csv", startDate, endDate, null, null);
    }

    @Override
    @Transactional
    public Long exportStatisticsToPdf(Long bookId, Long userId, LocalDate startDate, LocalDate endDate) {
        return createExportRecordAndExecute(bookId, userId, "statistics", "pdf", startDate, endDate, null, null);
    }

    /**
     * 异步执行导出
     */
    @Async
    public void executeExportAsync(Long recordId, Long userId, ExportRequest request) {
        try {
            // 更新状态为处理中
            ExportRecord record = exportRecordRepository.selectById(recordId);
            record.setStatus(ExportStatus.PROCESSING.getCode());
            exportRecordRepository.updateById(record);

            // 根据类型执行导出
            byte[] fileData;
            if ("transaction".equals(request.getType())) {
                fileData = exportTransactions(request.getBookId(), request.getStartDate(), request.getEndDate(), request.getCategoryId(), request.getFormat());
            } else if ("budget".equals(request.getType())) {
                fileData = exportBudget(request.getBookId(), request.getPeriod(), request.getFormat());
            } else if ("statistics".equals(request.getType())) {
                fileData = exportStatistics(request.getBookId(), request.getStartDate(), request.getEndDate(), request.getFormat());
            } else {
                throw new BusinessException("不支持的导出类型：" + request.getType());
            }

            // 更新记录（将文件以 Base64 形式临时存储，待 MinIO 集成后改为上传）
            record.setStatus(ExportStatus.COMPLETED.getCode());
            record.setFileSize((long) fileData.length);
            String base64Data = Base64.getEncoder().encodeToString(fileData);
            record.setFilePath("data:application/octet-stream;base64," + base64Data);
            exportRecordRepository.updateById(record);

            log.info("导出任务完成：recordId={}, fileSize={}", recordId, fileData.length);
        } catch (Exception e) {
            log.error("导出任务失败：recordId={}, error={}", recordId, e.getMessage(), e);
            ExportRecord record = exportRecordRepository.selectById(recordId);
            record.setStatus(ExportStatus.FAILED.getCode());
            record.setErrorMessage(e.getMessage());
            exportRecordRepository.updateById(record);
        }
    }

    /**
     * 导出交易数据
     */
    private byte[] exportTransactions(Long bookId, LocalDate startDate, LocalDate endDate, Long categoryId, String format) throws IOException {
        // 查询交易数据
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId);
        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }
        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        // 转换为 Map 列表
        List<Map<String, Object>> data = new ArrayList<>();
        for (Transaction tx : transactions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("日期", tx.getTransactionDate());
            row.put("类型", tx.getType() == 1 ? "支出" : "收入");
            row.put("分类", tx.getCategoryId());
            row.put("金额", tx.getAmount());
            row.put("账户", tx.getAccountId());
            row.put("备注", tx.getDescription());
            data.add(row);
        }

        String[] headers = {"日期", "类型", "分类", "金额", "账户", "备注"};

        if ("csv".equalsIgnoreCase(format)) {
            return CsvExportUtil.exportToCsv(data, headers);
        } else {
            return ExcelExportUtil.exportTransactions(data, headers);
        }
    }

    /**
     * 导出预算数据
     */
    private byte[] exportBudget(Long bookId, String period, String format) throws IOException {
        // 查询预算数据
        List<Budget> budgets = budgetRepository.findByBookId(bookId, period, null);

        List<Map<String, Object>> data = new ArrayList<>();
        for (Budget budget : budgets) {
            // 计算已用金额
            BigDecimal usedAmount = budgetRepository.sumExpensesByBookIdAndCategoryIdAndDateRange(
                    bookId,
                    budget.getCategoryId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );

            // 计算进度百分比
            BigDecimal progress = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? usedAmount.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2, BigDecimal.ROUND_HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("预算名称", budget.getName());
            row.put("预算金额", budget.getAmount());
            row.put("已用金额", usedAmount);
            row.put("进度", progress.setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
            row.put("状态", translateBudgetStatus(budget.getStatus()));
            data.add(row);
        }

        String[] headers = {"预算名称", "预算金额", "已用金额", "进度", "状态"};
        String title = "预算报表 - " + (period != null ? period : "全部");

        return exportToFile(data, headers, title, format);
    }

    /**
     * 导出统计数据
     */
    private byte[] exportStatistics(Long bookId, LocalDate startDate, LocalDate endDate, String format) throws IOException {
        // 按分类统计支出
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId);
        wrapper.eq(Transaction::getType, 2); // 支出
        if (startDate != null) {
            wrapper.ge(Transaction::getTransactionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Transaction::getTransactionDate, endDate);
        }
        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        // 按分类聚合
        Map<Long, BigDecimal> categoryAmounts = new HashMap<>();
        Map<Long, Long> categoryCounts = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Transaction tx : transactions) {
            Long categoryId = tx.getCategoryId();
            BigDecimal amount = tx.getAmount();
            categoryAmounts.merge(categoryId, amount, BigDecimal::add);
            categoryCounts.merge(categoryId, 1L, Long::sum);
            totalAmount = totalAmount.add(amount);
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categoryAmounts.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("分类 ID", entry.getKey());
            row.put("总支出", entry.getValue());
            row.put("百分比", totalAmount.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100)).divide(totalAmount, 2, BigDecimal.ROUND_HALF_UP) + "%"
                    : "0%");
            row.put("交易笔数", categoryCounts.get(entry.getKey()));
            data.add(row);
        }

        String[] headers = {"分类 ID", "总支出", "百分比", "交易笔数"};
        String dateRange = (startDate != null ? startDate.toString() : "开始") + " 至 " + (endDate != null ? endDate.toString() : "结束");
        String title = "统计报表 - " + dateRange;

        return exportToFile(data, headers, title, format);
    }

    /**
     * 根据格式导出到文件
     */
    private byte[] exportToFile(List<Map<String, Object>> data, String[] headers, String title, String format) throws IOException {
        if ("pdf".equalsIgnoreCase(format)) {
            return PdfExportUtil.exportTransactions(data, headers, title);
        } else if ("csv".equalsIgnoreCase(format)) {
            return CsvExportUtil.exportToCsv(data, headers);
        } else {
            return ExcelExportUtil.exportTransactions(data, headers);
        }
    }

    /**
     * 翻译预算状态
     */
    private String translateBudgetStatus(String status) {
        if (status == null) return "未知";
        return switch (status.toLowerCase()) {
            case "active" -> "进行中";
            case "completed" -> "已完成";
            case "overdue" -> "已逾期";
            default -> status;
        };
    }

    /**
     * 创建导出记录并执行
     */
    private Long createExportRecordAndExecute(Long bookId, Long userId, String type, String format, LocalDate startDate, LocalDate endDate, Long categoryId, String period) {
        ExportRequest request = ExportRequest.builder()
                .bookId(bookId)
                .type(type)
                .format(format)
                .startDate(startDate)
                .endDate(endDate)
                .categoryId(categoryId)
                .period(period)
                .build();

        ExportResponse response = createExportTask(userId, request);
        return response.getId();
    }

    /**
     * 创建导出记录并执行（无 period 参数重载）
     */
    private Long createExportRecordAndExecute(Long bookId, Long userId, String type, String format, LocalDate startDate, LocalDate endDate, Long categoryId) {
        return createExportRecordAndExecute(bookId, userId, type, format, startDate, endDate, categoryId, null);
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String type, String format) {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = switch (format.toLowerCase()) {
            case "excel" -> "xlsx";
            case "csv" -> "csv";
            case "pdf" -> "pdf";
            default -> "xlsx";
        };
        return String.format("export_%s_%s.%s", type, timestamp, extension);
    }

    /**
     * 构建响应对象
     */
    private ExportResponse buildResponse(ExportRecord record) {
        ExportStatus status = ExportStatus.fromCode(record.getStatus());
        return ExportResponse.builder()
                .id(record.getId())
                .exportType(record.getExportType())
                .fileFormat(record.getFileFormat())
                .fileName(record.getFileName())
                .fileSize(record.getFileSize())
                .downloadUrl(record.getFilePath()) // TODO: 生成 MinIO 下载 URL
                .status(record.getStatus())
                .statusName(status.getName())
                .errorMessage(record.getErrorMessage())
                .expireAt(record.getExpireAt())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
