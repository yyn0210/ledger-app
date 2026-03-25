package com.ledger.app.modules.export.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出响应
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportResponse {

    /**
     * 导出记录 ID
     */
    private Long id;

    /**
     * 导出类型
     */
    private String exportType;

    /**
     * 文件格式
     */
    private String fileFormat;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 下载 URL
     */
    private String downloadUrl;

    /**
     * 状态：1-等待生成 2-生成中 3-已完成 4-生成失败
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 过期时间
     */
    private LocalDateTime expireAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
