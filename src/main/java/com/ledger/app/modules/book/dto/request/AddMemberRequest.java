package com.ledger.app.modules.book.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 添加成员请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMemberRequest {

    /**
     * 用户手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 角色 (1-owner, 2-admin, 3-member)
     */
    private Integer role = 3;
}
