package com.ledger.app.modules.export.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 导出请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {

    /**
     * 账本 ID
     */
    @NotNull(message = "账本 ID 不能为空")
    private Long bookId;

    /**
     * 导出类型：transaction/budget/statistics
     */
    @NotBlank(message = "导出类型不能为空")
    private String type;

    /**
     * 文件格式：excel/csv/pdf
     */
    @NotBlank(message = "文件格式不能为空")
    private String format;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 分类 ID（可选，用于筛选）
     */
    private Long categoryId;

    /**
     * 预算周期（可选，用于预算导出）
     */
    private String period;
}
