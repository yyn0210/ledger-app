package com.ledger.app.modules.recurring.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.app.modules.recurring.dto.request.CreateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.request.UpdateRecurringBillRequest;
import com.ledger.app.modules.recurring.dto.response.RecurringBillResponse;
import com.ledger.app.modules.recurring.entity.RecurringBill;

import java.util.List;

/**
 * 周期账单服务接口
 *
 * @author Chisong
 * @since 2026-03-24
 */
public interface RecurringBillService {

    /**
     * 创建周期账单
     *
     * @param request 创建请求
     * @param userId  用户 ID
     * @return 周期账单 ID
     */
    Long create(CreateRecurringBillRequest request, Long userId);

    /**
     * 更新周期账单
     *
     * @param id      周期账单 ID
     * @param request 更新请求
     * @param userId  用户 ID
     */
    void update(Long id, UpdateRecurringBillRequest request, Long userId);

    /**
     * 删除周期账单
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     */
    void delete(Long id, Long userId);

    /**
     * 获取周期账单详情
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     * @return 周期账单详情
     */
    RecurringBillResponse getById(Long id, Long userId);

    /**
     * 分页查询周期账单列表
     *
     * @param bookId 账本 ID
     * @param userId 用户 ID
     * @param page   页码
     * @param size   每页数量
     * @return 周期账单列表
     */
    Page<RecurringBillResponse> pageByBookId(Long bookId, Long userId, Integer page, Integer size);

    /**
     * 手动执行周期账单
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     */
    void execute(Long id, Long userId);

    /**
     * 暂停周期账单
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     */
    void pause(Long id, Long userId);

    /**
     * 恢复周期账单
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     */
    void resume(Long id, Long userId);

    /**
     * 跳过本次执行
     *
     * @param id     周期账单 ID
     * @param userId 用户 ID
     */
    void skip(Long id, Long userId);

    /**
     * 自动执行到期的周期账单
     *
     * @return 执行数量
     */
    int autoExecutePending();
}
