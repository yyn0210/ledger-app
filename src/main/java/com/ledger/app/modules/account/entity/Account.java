package com.ledger.app.modules.account.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体类
 */
@Data
@TableName("accounts")
public class Account {

    /**
     * 账户 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账本 ID
     */
    private Long bookId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 账户名称
     */
    private String name;

    /**
     * 账户类型：1=现金 2=银行卡 3=信用卡 4=支付宝 5=微信 6=其他
     */
    private Integer type;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 货币代码
     */
    private String currency;

    /**
     * 图标 emoji
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 是否计入总资产：true=是 false=否
     */
    private Boolean isInclude;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除：0=未删除 1=已删除
     */
    @TableLogic
    private Integer deleted;
}
