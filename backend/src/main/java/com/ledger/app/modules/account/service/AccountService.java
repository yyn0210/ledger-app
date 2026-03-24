package com.ledger.app.modules.account.service;

import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.dto.response.AccountSummaryResponse;

import java.util.List;

/**
 * 账户服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface AccountService {

    /**
     * 获取账户列表
     *
     * @param bookId 账本 ID
     * @return 账户列表
     */
    List<AccountResponse> getAccountsByBookId(Long bookId);

    /**
     * 获取账户详情
     *
     * @param id 账户 ID
     * @param bookId 账本 ID
     * @return 账户信息
     */
    AccountResponse getAccountByIdAndBookId(Long id, Long bookId);

    /**
     * 创建账户
     *
     * @param request 创建请求
     * @return 账户信息
     */
    AccountResponse createAccount(CreateAccountRequest request);

    /**
     * 更新账户
     *
     * @param id 账户 ID
     * @param bookId 账本 ID
     * @param request 更新请求
     * @return 账户信息
     */
    AccountResponse updateAccount(Long id, Long bookId, UpdateAccountRequest request);

    /**
     * 删除账户
     *
     * @param id 账户 ID
     * @param bookId 账本 ID
     */
    void deleteAccount(Long id, Long bookId);

    /**
     * 账户汇总统计
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 汇总统计
     */
    AccountSummaryResponse getAccountSummary(Long bookId, Long userId);
}
