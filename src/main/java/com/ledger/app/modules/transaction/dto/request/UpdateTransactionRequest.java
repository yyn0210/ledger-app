package com.ledger.app.modules.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 更新交易记录请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Schema(description = "更新交易记录请求")
public class UpdateTransactionRequest {

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "交易类型：1=收入 2=支出 3=转账", example = "2")
    private Integer type;

    @Schema(description = "金额", example = "80.00")
    private BigDecimal amount;

    @Schema(description = "分类 ID", example = "2")
    private Long categoryId;

    @Schema(description = "账户 ID", example = "1")
    private Long accountId;

    @Schema(description = "标题", example = "晚餐")
    private String title;

    @Schema(description = "描述", example = "更新后的描述")
    private String description;

    @Schema(description = "交易日期", example = "2026-03-24")
    private LocalDate transactionDate;

    @Schema(description = "地点", example = "北京市朝阳区")
    private String location;

    @Schema(description = "商户", example = "XX 餐厅")
    private String merchant;

    @Schema(description = "标签", example = "[\"聚餐\"]")
    private List<String> tags;

    @Schema(description = "图片 URL", example = "[\"https://...\"]")
    private List<String> imageUrls;
}
