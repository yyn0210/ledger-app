package com.ledger.app.modules.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 交易记录分页响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "交易记录分页响应")
public class TransactionPageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "交易列表")
    private List<TransactionResponse> list;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Long page;

    @Schema(description = "每页数量", example = "20")
    private Long size;

    @Schema(description = "总页数", example = "5")
    private Long totalPages;

    /**
     * 创建分页响应
     */
    public static TransactionPageResponse of(List<TransactionResponse> list, long total, long page, long size) {
        return TransactionPageResponse.builder()
                .list(list)
                .total(total)
                .page(page)
                .size(size)
                .totalPages((total + size - 1) / size)
                .build();
    }
}
