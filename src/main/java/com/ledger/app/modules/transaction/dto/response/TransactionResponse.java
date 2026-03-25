package com.ledger.app.modules.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ledger.app.modules.transaction.entity.Transaction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易记录响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "交易记录响应")
public class TransactionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "交易 ID", example = "1")
    private Long id;

    @Schema(description = "账本 ID", example = "1")
    private Long bookId;

    @Schema(description = "交易类型：1=收入 2=支出 3=转账", example = "2")
    private Integer type;

    @Schema(description = "交易类型名称", example = "支出")
    private String typeName;

    @Schema(description = "金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "分类 ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "餐饮")
    private String categoryName;

    @Schema(description = "账户 ID", example = "1")
    private Long accountId;

    @Schema(description = "账户名称", example = "微信支付")
    private String accountName;

    @Schema(description = "标题", example = "午餐")
    private String title;

    @Schema(description = "描述", example = "公司附近的面馆")
    private String description;

    @Schema(description = "交易日期", example = "2026-03-24")
    private LocalDate transactionDate;

    @Schema(description = "地点", example = "北京市朝阳区")
    private String location;

    @Schema(description = "商户", example = "XX 面馆")
    private String merchant;

    @Schema(description = "标签", example = "[\"工作餐\"]")
    private List<String> tags;

    @Schema(description = "图片 URL", example = "[\"https://...\"]")
    private List<String> imageUrls;

    @Schema(description = "创建时间", example = "2026-03-24T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2026-03-24T12:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     */
    public static TransactionResponse fromEntity(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .bookId(transaction.getBookId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .categoryId(transaction.getCategoryId())
                .accountId(transaction.getAccountId())
                .title(transaction.getTitle())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .location(transaction.getLocation())
                .merchant(transaction.getMerchant())
                .tags(transaction.getTags() != null ? parseJsonArray(transaction.getTags()) : null)
                .imageUrls(transaction.getImageUrls() != null ? parseJsonArray(transaction.getImageUrls()) : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    /**
     * 解析 JSON 数组字符串
     */
    private static List<String> parseJsonArray(String json) {
        // 简单实现，实际项目中应使用 Jackson 解析
        if (json == null || json.isEmpty()) {
            return null;
        }
        // 移除 [ ] 和引号
        String content = json.replaceAll("[\\[\\]\"]", "");
        if (content.isEmpty()) {
            return null;
        }
        return List.of(content.split(","));
    }
}
