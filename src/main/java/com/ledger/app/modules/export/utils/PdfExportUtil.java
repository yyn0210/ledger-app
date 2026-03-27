package com.ledger.app.modules.export.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF 导出工具类
 *
 * @author Chisong
 * @since 2026-03-24
 */
public class PdfExportUtil {

    /**
     * 导出交易记录为 PDF
     *
     * @param transactions 交易数据
     * @param headers 表头
     * @param title 报表标题
     * @return PDF 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportTransactions(List<Map<String, Object>> transactions, String[] headers, String title) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);

        // 添加标题
        Paragraph titleParagraph = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(titleParagraph);

        // 添加生成时间
        String generatedTime = "生成时间：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        document.add(new Paragraph(generatedTime)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(15));

        // 创建表格
        float[] columnWidths = new float[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = 1.0f;
        }
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .useAllAvailableWidth();

        // 添加表头
        for (String header : headers) {
            Cell headerCell = new Cell().add(new Paragraph(header))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(headerCell);
        }

        // 添加数据行
        for (Map<String, Object> transaction : transactions) {
            for (String header : headers) {
                Object value = transaction.get(header);
                String cellValue = value != null ? value.toString() : "";
                Cell cell = new Cell().add(new Paragraph(cellValue))
                        .setTextAlignment(TextAlignment.CENTER);
                table.addCell(cell);
            }
        }

        document.add(table);

        // 添加汇总信息
        if (!transactions.isEmpty()) {
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("共 " + transactions.size() + " 条记录")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        document.close();
        return baos.toByteArray();
    }

    /**
     * 导出预算报表为 PDF
     *
     * @param budgets 预算数据
     * @param headers 表头
     * @param title 报表标题
     * @return PDF 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportBudgets(List<Map<String, Object>> budgets, String[] headers, String title) throws IOException {
        return exportTransactions(budgets, headers, title);
    }

    /**
     * 导出统计报表为 PDF
     *
     * @param statistics 统计数据
     * @param headers 表头
     * @param title 报表标题
     * @return PDF 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportStatistics(List<Map<String, Object>> statistics, String[] headers, String title) throws IOException {
        return exportTransactions(statistics, headers, title);
    }
}
