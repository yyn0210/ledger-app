package com.ledger.app.modules.budget.service.impl;

import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.budget.dto.request.CreateBudgetRequest;
import com.ledger.app.modules.budget.dto.request.UpdateBudgetRequest;
import com.ledger.app.modules.budget.dto.response.BudgetAlertResponse;
import com.ledger.app.modules.budget.dto.response.BudgetDetailResponse;
import com.ledger.app.modules.budget.dto.response.BudgetResponse;
import com.ledger.app.modules.budget.dto.response.BudgetSummaryResponse;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.enums.BudgetPeriod;
import com.ledger.app.modules.budget.enums.BudgetStatus;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.budget.service.BudgetService;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.transaction.entity.Transaction;
import com.ledger.app.modules.transaction.repository.TransactionRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 预算服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<BudgetResponse> getBudgets(Long bookId, Long userId, String period, String status) {
        log.info("获取预算列表，bookId: {}, userId: {}, period: {}, status: {}", bookId, userId, period, status);

        List<Budget> budgets;
        if (period != null) {
            budgets = budgetRepository.selectByBookIdAndUserIdAndPeriod(bookId, userId, period);
        } else if (status != null) {
            budgets = budgetRepository.selectByBookIdAndUserIdAndStatus(bookId, userId, status);
        } else {
            budgets = budgetRepository.selectByBookIdAndUserId(bookId, userId);
        }

        return budgets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetDetailResponse getBudgetDetail(Long id, Long bookId, Long userId) {
        log.info("获取预算详情，id: {}, bookId: {}", id, bookId);

        Budget budget = budgetRepository.selectByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }

        return convertToDetailResponse(budget);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBudget(CreateBudgetRequest request) {
        log.info("创建预算，request: {}", request);

        // 验证预算周期
        BudgetPeriod.fromCode(request.getPeriod());

        // 验证分类是否存在（如果提供了 categoryId）
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.selectByIdAndBookId(request.getCategoryId(), request.getBookId());
            if (category == null) {
                throw new BusinessException("分类不存在");
            }
        }

        // 验证日期范围
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        // 创建预算
        Budget budget = new Budget();
        budget.setBookId(request.getBookId());
        budget.setUserId(request.getUserId());
        budget.setName(request.getName());
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setAlertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : new BigDecimal("80.00"));
        budget.setStatus(BudgetStatus.ACTIVE.getCode());
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());

        budgetRepository.insert(budget);

        log.info("预算创建成功，id: {}", budget.getId());
        return budget.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBudget(Long id, UpdateBudgetRequest request, Long bookId, Long userId) {
        log.info("更新预算，id: {}, bookId: {}", id, bookId);

        Budget budget = budgetRepository.selectByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            budget.setName(request.getName());
        }
        if (request.getAmount() != null) {
            budget.setAmount(request.getAmount());
        }
        if (request.getAlertThreshold() != null) {
            budget.setAlertThreshold(request.getAlertThreshold());
        }
        budget.setUpdatedAt(LocalDateTime.now());

        budgetRepository.updateById(budget);

        log.info("预算更新成功，id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBudget(Long id, Long bookId, Long userId) {
        log.info("删除预算，id: {}, bookId: {}", id, bookId);

        Budget budget = budgetRepository.selectByIdAndBookId(id, bookId);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }

        // 软删除
        budgetRepository.deleteById(id);

        log.info("预算删除成功，id: {}", id);
    }

    @Override
    public BudgetSummaryResponse getBudgetSummary(Long bookId, Long userId, String period) {
        log.info("获取预算汇总统计，bookId: {}, userId: {}, period: {}", bookId, userId, period);

        BudgetSummaryResponse response = new BudgetSummaryResponse();

        // 获取预算列表
        List<Budget> budgets;
        if (period != null) {
            budgets = budgetRepository.selectByBookIdAndUserIdAndPeriod(bookId, userId, period);
        } else {
            budgets = budgetRepository.selectByBookIdAndUserId(bookId, userId);
        }

        if (budgets.isEmpty()) {
            response.setTotalBudget(BigDecimal.ZERO);
            response.setTotalSpent(BigDecimal.ZERO);
            response.setOverallProgress(BigDecimal.ZERO);
            response.setBudgetCount(0);
            response.setOverBudgetCount(0);
            response.setWarningCount(0);
            response.setByCategory(new ArrayList<>());
            return response;
        }

        // 计算汇总数据
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        int overBudgetCount = 0;
        int warningCount = 0;
        List<BudgetSummaryResponse.CategoryBudget> byCategory = new ArrayList<>();

        for (Budget budget : budgets) {
            // 计算支出和进度
            BigDecimal spentAmount = calculateSpentAmount(budget);
            BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);

            // 汇总
            totalBudget = totalBudget.add(budget.getAmount());
            totalSpent = totalSpent.add(spentAmount);

            // 检查状态
            if (progress.compareTo(new BigDecimal("100")) > 0) {
                overBudgetCount++;
            } else if (progress.compareTo(budget.getAlertThreshold()) > 0) {
                warningCount++;
            }

            // 按分类分组
            BudgetSummaryResponse.CategoryBudget categoryBudget = new BudgetSummaryResponse.CategoryBudget();
            categoryBudget.setCategoryId(budget.getCategoryId());
            categoryBudget.setBudgetAmount(budget.getAmount());
            categoryBudget.setSpentAmount(spentAmount);
            categoryBudget.setProgress(progress);
            categoryBudget.setStatus(determineStatus(progress, budget.getEndDate()));

            // 获取分类名称
            if (budget.getCategoryId() != null) {
                Category category = categoryRepository.selectById(budget.getCategoryId());
                if (category != null) {
                    categoryBudget.setCategoryName(category.getName());
                }
            } else {
                categoryBudget.setCategoryName("总预算");
            }

            byCategory.add(categoryBudget);
        }

        // 计算总体进度
        BigDecimal overallProgress = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        response.setTotalBudget(totalBudget);
        response.setTotalSpent(totalSpent);
        response.setOverallProgress(overallProgress);
        response.setBudgetCount(budgets.size());
        response.setOverBudgetCount(overBudgetCount);
        response.setWarningCount(warningCount);
        response.setByCategory(byCategory);

        log.info("预算汇总统计完成，totalBudget: {}, totalSpent: {}, overallProgress: {}",
                response.getTotalBudget(), response.getTotalSpent(), response.getOverallProgress());
        return response;
    }

    @Override
    public List<BudgetAlertResponse> checkBudgetAlerts(Long bookId, Long userId) {
        log.info("检查预算预警，bookId: {}, userId: {}", bookId, userId);

        List<Budget> budgets = budgetRepository.selectByBookIdAndUserId(bookId, userId);
        List<BudgetAlertResponse> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            BigDecimal spentAmount = calculateSpentAmount(budget);
            BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);

            BudgetAlertResponse alert = new BudgetAlertResponse();
            alert.setBudgetId(budget.getId());
            alert.setBudgetName(budget.getName());
            alert.setCategoryId(budget.getCategoryId());
            alert.setAmount(budget.getAmount());
            alert.setSpentAmount(spentAmount);
            alert.setProgress(progress);
            alert.setAlertThreshold(budget.getAlertThreshold());

            // 获取分类名称
            if (budget.getCategoryId() != null) {
                Category category = categoryRepository.selectById(budget.getCategoryId());
                if (category != null) {
                    alert.setCategoryName(category.getName());
                }
            }

            // 超支预警
            if (progress.compareTo(new BigDecimal("100")) > 0) {
                BigDecimal overAmount = progress.subtract(new BigDecimal("100"))
                        .multiply(budget.getAmount())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                alert.setAlertType("overdue");
                alert.setMessage(String.format("预算已超支 %.2f 元", overAmount.doubleValue()));
                alerts.add(alert);
            }
            // 预警线提醒
            else if (progress.compareTo(budget.getAlertThreshold()) > 0) {
                alert.setAlertType("warning");
                alert.setMessage(String.format("预算使用率已达 %.2f%%，超过预警线 %.2f%%",
                        progress.doubleValue(),
                        budget.getAlertThreshold().doubleValue()));
                alerts.add(alert);
            }
        }

        log.info("预算预警检查完成，alertCount: {}", alerts.size());
        return alerts;
    }

    /**
     * 计算预算周期内的实际支出
     */
    private BigDecimal calculateSpentAmount(Budget budget) {
        if (budget.getCategoryId() != null) {
            // 分类预算
            return budgetRepository.sumByBookIdAndCategoryIdAndDateRange(
                    budget.getBookId(),
                    budget.getCategoryId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
        } else {
            // 总预算
            return budgetRepository.sumTotalExpenseByDateRange(
                    budget.getBookId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
        }
    }

    /**
     * 计算进度百分比
     */
    private BigDecimal calculateProgress(BigDecimal budgetAmount, BigDecimal spentAmount) {
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(new BigDecimal("100"))
                .divide(budgetAmount, 2, RoundingMode.HALF_UP);
    }

    /**
     * 确定预算状态
     */
    private String determineStatus(BigDecimal progress, LocalDate endDate) {
        if (progress.compareTo(new BigDecimal("100")) > 0) {
            return BudgetStatus.OVERDUE.getCode();
        } else if (LocalDate.now().isAfter(endDate)) {
            return BudgetStatus.COMPLETED.getCode();
        } else {
            return BudgetStatus.ACTIVE.getCode();
        }
    }

    /**
     * 转换 Entity 到 Response
     */
    private BudgetResponse convertToResponse(Budget budget) {
        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setBookId(budget.getBookId());
        response.setName(budget.getName());
        response.setCategoryId(budget.getCategoryId());
        response.setAmount(budget.getAmount());
        response.setPeriod(budget.getPeriod());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setAlertThreshold(budget.getAlertThreshold());
        response.setCreatedAt(budget.getCreatedAt());
        response.setUpdatedAt(budget.getUpdatedAt());

        // 计算支出和进度
        BigDecimal spentAmount = calculateSpentAmount(budget);
        BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);
        response.setSpentAmount(spentAmount);
        response.setProgress(progress);

        // 更新并设置状态
        String status = determineStatus(progress, budget.getEndDate());
        response.setStatus(status);

        // 获取分类名称
        if (budget.getCategoryId() != null) {
            Category category = categoryRepository.selectById(budget.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        return response;
    }

    /**
     * 转换 Entity 到 DetailResponse
     */
    private BudgetDetailResponse convertToDetailResponse(Budget budget) {
        BudgetDetailResponse response = new BudgetDetailResponse();
        response.setId(budget.getId());
        response.setBookId(budget.getBookId());
        response.setName(budget.getName());
        response.setCategoryId(budget.getCategoryId());
        response.setAmount(budget.getAmount());
        response.setPeriod(budget.getPeriod());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());
        response.setAlertThreshold(budget.getAlertThreshold());
        response.setCreatedAt(budget.getCreatedAt());
        response.setUpdatedAt(budget.getUpdatedAt());

        // 计算支出和进度
        BigDecimal spentAmount = calculateSpentAmount(budget);
        BigDecimal progress = calculateProgress(budget.getAmount(), spentAmount);
        response.setSpentAmount(spentAmount);
        response.setProgress(progress);

        // 更新并设置状态
        String status = determineStatus(progress, budget.getEndDate());
        response.setStatus(status);

        // 获取分类名称
        if (budget.getCategoryId() != null) {
            Category category = categoryRepository.selectById(budget.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        // 获取相关交易记录
        if (budget.getCategoryId() != null) {
            LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Transaction::getBookId, budget.getBookId())
                    .eq(Transaction::getCategoryId, budget.getCategoryId())
                    .eq(Transaction::getType, 2) // 只统计支出
                    .ge(Transaction::getTransactionDate, budget.getStartDate())
                    .le(Transaction::getTransactionDate, budget.getEndDate())
                    .eq(Transaction::getDeleted, 0)
                    .orderByDesc(Transaction::getTransactionDate)
                    .last("LIMIT 10"); // 只取最近 10 条

            List<Transaction> transactions = transactionRepository.selectList(wrapper);
            List<BudgetDetailResponse.TransactionInfo> transactionInfos = transactions.stream()
                    .map(tx -> {
                        BudgetDetailResponse.TransactionInfo info = new BudgetDetailResponse.TransactionInfo();
                        info.setId(tx.getId());
                        info.setAmount(tx.getAmount());
                        info.setTitle(tx.getTitle());
                        info.setTransactionDate(tx.getTransactionDate());
                        return info;
                    })
                    .collect(Collectors.toList());
            response.setTransactions(transactionInfos);
        }

        return response;
    }
}
