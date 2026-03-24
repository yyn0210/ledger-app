package com.ledger.app.modules.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Data
@TableName("accounts")
@Schema(description = "账户信息")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "账户 ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "账本 ID")
    @TableField("book_id")
    private Long bookId;

    @Schema(description = "用户 ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "账户名称")
    private String name;

    @Schema(description = "账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他")
    private Integer type;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "货币代码")
    private String currency;

    @Schema(description = "图标 emoji")
    private String icon;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "是否计入总资产：0=否 1=是")
    @TableField("is_include")
    private Integer isInclude;

    @Schema(description = "排序顺序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Schema(description = "逻辑删除：0=未删除 1=已删除")
    @TableLogic
    private Integer deleted;
}
