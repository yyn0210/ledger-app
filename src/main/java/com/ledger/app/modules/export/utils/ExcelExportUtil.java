package com.ledger.app.modules.export.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel 导出工具类
 *
 * @author Chisong
 * @since 2026-03-24
 */
public class ExcelExportUtil {

    /**
     * 导出交易记录为 Excel
     *
     * @param transactions 交易数据
     * @param headers 表头
     * @return Excel 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportTransactions(List<Map<String, Object>> transactions, String[] headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("交易记录");

            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;
                for (String header : headers) {
                    Cell cell = row.createCell(colNum++);
                    Object value = transaction.get(header);
                    if (value != null) {
                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * 导出预算报表为 Excel
     *
     * @param budgets 预算数据
     * @param headers 表头
     * @return Excel 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportBudgets(List<Map<String, Object>> budgets, String[] headers) throws IOException {
        return exportTransactions(budgets, headers);
    }

    /**
     * 导出统计报表为 Excel
     *
     * @param statistics 统计数据
     * @param headers 表头
     * @return Excel 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportStatistics(List<Map<String, Object>> statistics, String[] headers) throws IOException {
        return exportTransactions(statistics, headers);
    }
}
