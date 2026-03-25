package com.ledger.app.modules.export.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CSV 导出工具类
 *
 * @author Chisong
 * @since 2026-03-24
 */
public class CsvExportUtil {

    /**
     * 导出为 CSV
     *
     * @param data 数据列表
     * @param headers 表头
     * @return CSV 文件字节数组
     * @throws IOException IO 异常
     */
    public static byte[] exportToCsv(List<Map<String, Object>> data, String[] headers) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 写入 BOM（UTF-8）
        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        // 写入表头
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            sb.append(escapeCsv(headers[i]));
            if (i < headers.length - 1) {
                sb.append(",");
            }
        }
        sb.append("\r\n");

        // 写入数据
        for (Map<String, Object> row : data) {
            for (int i = 0; i < headers.length; i++) {
                Object value = row.get(headers[i]);
                sb.append(escapeCsv(value != null ? value.toString() : ""));
                if (i < headers.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("\r\n");
        }

        baos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        return baos.toByteArray();
    }

    /**
     * 转义 CSV 字段
     */
    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        // 如果包含逗号、双引号或换行符，需要用双引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
