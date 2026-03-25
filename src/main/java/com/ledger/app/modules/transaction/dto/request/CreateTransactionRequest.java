package com.ledger.app.modules.transaction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
<<<<<<< HEAD
=======
import lombok.Builder;
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建交易记录请求
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
<<<<<<< HEAD
=======
@Builder
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
@Schema(description = "创建交易记录请求")
public class CreateTransactionRequest {

    @NotNull(message = "账本 ID 不能为空")
    @Schema(description = "账本 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotNull(message = "交易类型不能为空")
    @Schema(description = "交易类型：1=收入 2=支出 3=转账", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @NotNull(message = "金额不能为空")
    @Schema(description = "金额", example = "50.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @NotNull(message = "账户 ID 不能为空")
    @Schema(description = "账户 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long accountId;

    @Schema(description = "目标账户 ID（转账时必填）", example = "2")
    private Long toAccountId;

    @Schema(description = "标题", example = "午餐")
    private String title;

    @Schema(description = "描述", example = "公司附近的面馆")
    private String description;

    @NotNull(message = "交易日期不能为空")
    @Schema(description = "交易日期", example = "2026-03-24", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate transactionDate;

    @Schema(description = "地点", example = "北京市朝阳区")
    private String location;

    @Schema(description = "商户", example = "XX 面馆")
    private String merchant;

    @Schema(description = "标签", example = "[\"工作餐\"]")
    private List<String> tags;

    @Schema(description = "图片 URL", example = "[\"https://...\"]")
    private List<String> imageUrls;
}
