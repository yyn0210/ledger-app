package com.ledger.app.modules.budget.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.enums.BudgetStatus;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.budget.service.BudgetService;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.transaction.dto.response.TransactionResponse;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.ledger.app.modules.websocket.service.WebSocketMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final WebSocketMessageService webSocketMessageService;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long bookId, String period, String status) {
        List<Budget> budgets = budgetRepository.findByBookId(bookId, period, status);
        return budgets.stream()
                .map(this::buildBudgetResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDetailResponse getBudgetDetail(Long id, Long bookId) {
        Budget budget = budgetRepository.findByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在或无权访问");
        }

        // 计算执行进度
        BigDecimal spentAmount = calculateSpentAmount(budget);
        BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);

        // 获取相关交易列表
        List<TransactionResponse> transactions = getRelatedTransactions(budget);

        // 构建响应
        BudgetDetailResponse response = BudgetDetailResponse.builder()
                .id(budget.getId())
                .bookId(budget.getBookId())
                .name(budget.getName())
                .categoryId(budget.getCategoryId())
                .categoryName(getCategoryName(budget.getCategoryId()))
                .amount(budget.getAmount())
                .spentAmount(spentAmount)
                .progress(progress)
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .alertThreshold(budget.getAlertThreshold())
                .status(budget.getStatus())
                .transactions(transactions)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();

        // 更新预算状态
        updateBudgetStatus(budget, spentAmount);

        return response;
    }

    @Override
    @Transactional
    public BudgetResponse createBudget(Long userId, CreateBudgetRequest request) {
        Budget budget = new Budget();
        budget.setBookId(request.getBookId());
        budget.setUserId(userId);
        budget.setCategoryId(request.getCategoryId());
        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : new BigDecimal("80.00"));
        budget.setStatus(BudgetStatus.ACTIVE.getCode());
        budget.setDeleted(0);
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());

        budgetRepository.insert(budget);
        log.info("创建预算成功：userId={}, budgetId={}, name={}", userId, budget.getId(), budget.getName());

        return buildBudgetResponse(budget);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, Long bookId, Long userId, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在或无权访问");
        }

        if (request.getBookId() != null) {
            budget.setBookId(request.getBookId());
        }
        if (request.getName() != null) {
            budget.setName(request.getName());
        }
        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }
        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }
        if (request.getStartDate() != null) {
            budget.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            budget.setEndDate(request.getEndDate());
        }

        budget.setUpdatedAt(LocalDateTime.now());
        budgetRepository.updateById(budget);
        log.info("更新预算成功：userId={}, budgetId={}", userId, id);

        return buildBudgetResponse(budget);
    }

    @Override
    @Transactional
    public void deleteBudget(Long id, Long bookId) {
        Budget budget = budgetRepository.findByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在或无权访问");
        }

        // 软删除
        budget.setDeleted(1);
        budget.setUpdatedAt(LocalDateTime.now());
        budgetRepository.updateById(budget);
        log.info("删除预算成功：budgetId={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetSummaryResponse getBudgetSummary(Long bookId, String period) {
        List<Budget> budgets = budgetRepository.findByBookId(bookId, period, null);

        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        int overBudgetCount = 0;
        int warningCount = 0;
        List<BudgetSummaryResponse.CategoryBudget> byCategory = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spentAmount = calculateSpentAmount(budget);
            BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);

            totalBudget = totalBudget.add(budget.getAmount());
            totalSpent = totalSpent.add(spentAmount);

            // 检查超支和预警
            if (progress.compareTo(new BigDecimal("100")) > 0) {
                overBudgetCount++;
            } else if (progress.compareTo(budget.getAlertThreshold()) > 0) {
                warningCount++;
            }

            // 按分类统计
            BudgetSummaryResponse.CategoryBudget categoryBudget = BudgetSummaryResponse.CategoryBudget.builder()
                    .categoryId(budget.getCategoryId())
                    .categoryName(getCategoryName(budget.getCategoryId()))
                    .budgetAmount(budget.getAmount())
                    .spentAmount(spentAmount)
                    .progress(progress)
                    .status(budget.getStatus())
                    .build();
            byCategory.add(categoryBudget);
        }

        BigDecimal overallProgress = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return BudgetSummaryResponse.builder()
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .overallProgress(overallProgress)
                .budgetCount(budgets.size())
                .overBudgetCount(overBudgetCount)
                .warningCount(warningCount)
                .byCategory(byCategory)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgetAlerts(Long bookId) {
        List<Budget> budgets = budgetRepository.findByBookId(bookId, null, null);
        List<BudgetResponse> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spentAmount = calculateSpentAmount(budget);
            BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);

            // 超支预警
            if (progress.compareTo(new BigDecimal("100")) > 0) {
                BudgetResponse response = buildBudgetResponse(budget);
                response.setSpentAmount(spentAmount);
                response.setProgress(progress);
                alerts.add(response);
            }
            // 预警线提醒
            else if (progress.compareTo(budget.getAlertThreshold()) > 0) {
                BudgetResponse response = buildBudgetResponse(budget);
                response.setSpentAmount(spentAmount);
                response.setProgress(progress);
                alerts.add(response);
            }
        }

        return alerts;
    }

    /**
     * 计算实际支出
     */
    private BigDecimal calculateSpentAmount(Budget budget) {
        return budgetRepository.sumExpensesByBookIdAndCategoryIdAndDateRange(
                budget.getBookId(),
                budget.getCategoryId(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }

    /**
     * 计算执行进度
     */
    private BigDecimal calculateProgress(BigDecimal budgetAmount, BigDecimal spentAmount) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(new BigDecimal("100"))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP);
    }

    /**
     * 更新预算状态
     */
    private void updateBudgetStatus(Budget budget, BigDecimal spentAmount) {
        BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);
        BigDecimal oldProgress = calculateProgress(budget.getAmount(), 
            budgetRepository.sumExpensesByBookIdAndCategoryIdAndDateRange(
                budget.getBookId(), budget.getCategoryId(),
                budget.getStartDate(), budget.getEndDate().minusDays(1)
            ));

        // 检查是否超支
        if (progress.compareTo(new BigDecimal("100")) > 0) {
            budget.setStatus(BudgetStatus.OVERDUE.getCode());
            // 发送超支通知
            sendBudgetOverdueNotification(budget.getUserId(), budget, progress);
        }
        // 检查是否周期结束
        else if (LocalDate.now().isAfter(budget.getEndDate())) {
            budget.setStatus(BudgetStatus.COMPLETED.getCode());
        }
        // 检查是否达到预警线（且之前未达到）
        else if (progress.compareTo(budget.getAlertThreshold()) > 0 && 
                 oldProgress.compareTo(budget.getAlertThreshold()) <= 0) {
            budget.setStatus(BudgetStatus.ACTIVE.getCode());
            // 发送预警通知
            sendBudgetAlertNotification(budget.getUserId(), budget, progress);
        }
        // 否则为进行中
        else {
            budget.setStatus(BudgetStatus.ACTIVE.getCode());
        }

        budgetRepository.updateById(budget);
    }

    /**
     * 获取相关交易列表
     */
    private List<TransactionResponse> getRelatedTransactions(Budget budget) {
        // 简化实现：获取预算周期内的相关交易
        // 实际项目中应使用更复杂的查询
        return new ArrayList<>();
    }

    /**
     * 构建预算响应
     */
    private BudgetResponse buildBudgetResponse(Budget budget) {
        BudgetResponse response = BudgetResponse.builder()
                .id(budget.getId())
                .bookId(budget.getBookId())
                .name(budget.getName())
                .categoryId(budget.getCategoryId())
                .categoryName(getCategoryName(budget.getCategoryId()))
                .amount(budget.getAmount())
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .alertThreshold(budget.getAlertThreshold())
                .status(budget.getStatus())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();

        // 计算执行进度
        BigDecimal spentAmount = calculateSpentAmount(budget);
        response.setSpentAmount(spentAmount);
        response.setProgress(calculateProgress(budget.getAmount(), spentAmount));

        return response;
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "总预算";
        }
        Category category = categoryRepository.selectById(categoryId);
        return category != null ? category.getName() : "未知分类";
    }

    /**
     * 发送预算预警通知
     */
    private void sendBudgetAlertNotification(Long userId, Budget budget, BigDecimal progress) {
        try {
            webSocketMessageService.sendBudgetAlert(
                userId,
                budget.getBookId(),
                budget.getId(),
                budget.getName(),
                progress
            );
            log.info("发送预算预警通知：userId={}, budgetId={}, progress={}% ", userId, budget.getId(), progress);
        } catch (Exception e) {
            log.error("发送预算预警 WebSocket 通知失败：error={}", e.getMessage());
        }
    }

    /**
     * 发送预算超支通知
     */
    private void sendBudgetOverdueNotification(Long userId, Budget budget, BigDecimal progress) {
        try {
            webSocketMessageService.sendBudgetOverdue(
                userId,
                budget.getBookId(),
                budget.getId(),
                budget.getName(),
                progress
            );
            log.info("发送预算超支通知：userId={}, budgetId={}, progress={}% ", userId, budget.getId(), progress);
        } catch (Exception e) {
            log.error("发送预算超支 WebSocket 通知失败：error={}", e.getMessage());
        }
    }
}
