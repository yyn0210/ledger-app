package com.ledger.app.modules.account.service;

import com.ledger.app.modules.account.dto.request.CreateAccountRequest;
import com.ledger.app.modules.account.dto.request.UpdateAccountRequest;
import com.ledger.app.modules.account.dto.response.AccountResponse;
import com.ledger.app.modules.account.dto.response.AccountSummaryResponse;

import java.util.List;

/**
 * 账户服务接口
 */
public interface AccountService {

    /**
     * 获取账户列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 账户列表
     */
    List<AccountResponse> getAccounts(Long bookId, Long userId);

    /**
     * 获取账户详情
     *
     * @param id     账户 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @return 账户详情
     */
    AccountResponse getAccount(Long id, Long bookId, Long userId);

    /**
     * 创建账户
     *
     * @param request 创建请求
     * @return 账户 ID
     */
    Long createAccount(CreateAccountRequest request);

    /**
     * 更新账户
     *
     * @param id      账户 ID
     * @param request 更新请求
     * @param bookId  账本 ID
     * @param userId  用户 ID
     */
    void updateAccount(Long id, UpdateAccountRequest request, Long bookId, Long userId);

    /**
     * 删除账户
     *
     * @param id     账户 ID
     * @param bookId 账本 ID
     * @param userId 用户 ID
     */
    void deleteAccount(Long id, Long bookId, Long userId);

    /**
     * 获取账户汇总统计
     *
     * @param userId 用户 ID
     * @return 账户汇总统计
     */
    AccountSummaryResponse getAccountSummary(Long userId);
}
