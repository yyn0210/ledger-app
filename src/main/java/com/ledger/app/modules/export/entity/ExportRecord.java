package com.ledger.app.modules.export.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导出记录实体
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("export_record")
public class ExportRecord {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 账本 ID
     */
    @TableField("book_id")
    private Long bookId;

    /**
     * 导出类型：transaction/budget/statistics
     */
    @TableField("export_type")
    private String exportType;

    /**
     * 文件格式：excel/csv/pdf
     */
    @TableField("file_format")
    private String fileFormat;

    /**
     * 文件路径（MinIO）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 状态：1-等待生成 2-生成中 3-已完成 4-生成失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 过期时间
     */
    @TableField("expire_at")
    private LocalDateTime expireAt;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
