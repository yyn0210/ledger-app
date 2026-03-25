package com.ledger.app.modules.statistics.service.impl;

import com.ledger.app.modules.account.entity.Account;
import com.ledger.app.modules.account.enums.AccountType;
import com.ledger.app.modules.account.repository.AccountRepository;
import com.ledger.app.modules.budget.entity.Budget;
import com.ledger.app.modules.budget.repository.BudgetRepository;
import com.ledger.app.modules.category.entity.Category;
import com.ledger.app.modules.category.repository.CategoryRepository;
import com.ledger.app.modules.statistics.dto.response.*;
import com.ledger.app.modules.statistics.service.StatisticsService;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 *
 * @author Chisong
 * @since 2026-03-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatisticsResponse> getExpenseByCategory(Long bookId, String startDate, String endDate) {
        return getCategoryStatistics(bookId, 2, startDate, endDate); // type=2 支出
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatisticsResponse> getIncomeByCategory(Long bookId, String startDate, String endDate) {
        return getCategoryStatistics(bookId, 1, startDate, endDate); // type=1 收入
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendResponse> getTrend(Long bookId, String trendType, String startDate, String endDate) {
        List<Transaction> transactions = queryTransactions(bookId, startDate, endDate);

        // 按周期分组统计
        Map<String, Map<Integer, BigDecimal>> periodAmounts = new LinkedHashMap<>();

        for (Transaction tx : transactions) {
            String period = getPeriod(tx.getTransactionDate(), trendType);
            periodAmounts.computeIfAbsent(period, k -> new HashMap<>());

            Map<Integer, BigDecimal> amounts = periodAmounts.get(period);
            amounts.merge(tx.getType(), tx.getAmount(), BigDecimal::add);
        }

        // 构建响应
        return periodAmounts.entrySet().stream()
                .map(entry -> {
                    Map<Integer, BigDecimal> amounts = entry.getValue();
                    BigDecimal income = amounts.getOrDefault(1, BigDecimal.ZERO);
                    BigDecimal expense = amounts.getOrDefault(2, BigDecimal.ZERO);
                    return TrendResponse.builder()
                            .period(entry.getKey())
                            .income(income)
                            .expense(expense)
                            .surplus(income.subtract(expense))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingResponse> getRanking(Long bookId, String rankingType, Integer limit, String startDate, String endDate) {
        int type = "expense".equals(rankingType) ? 2 : 1;
        List<CategoryStatisticsResponse> stats = getCategoryStatistics(bookId, type, startDate, endDate);

        // 排序并取 TOP N
        java.util.concurrent.atomic.AtomicInteger rank = new java.util.concurrent.atomic.AtomicInteger(0);
        return stats.stream()
                .sorted(Comparator.comparing(CategoryStatisticsResponse::getAmount).reversed())
                .limit(limit != null ? limit : 10)
                .map(s -> RankingResponse.builder()
                        .rank(rank.incrementAndGet())
                        .categoryId(s.getCategoryId())
                        .categoryName(s.getCategoryName())
                        .icon(s.getIcon())
                        .amount(s.getAmount())
                        .transactionCount(s.getTransactionCount())
                        .percentage(s.getPercentage())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetsSummaryResponse getAssetsSummary(Long bookId, Long userId) {
        List<Account> accounts = accountRepository.findByBookId(bookId);

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        Map<Integer, BigDecimal> byType = new HashMap<>();

        for (Account account : accounts) {
            BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
            Integer type = account.getType();

            // 信用卡等负债账户
            if (type != null && type == AccountType.CREDIT_CARD.getCode()) {
                totalLiabilities = totalLiabilities.add(balance.abs());
            } else {
                totalAssets = totalAssets.add(balance);
            }

            byType.merge(type, balance, BigDecimal::add);
        }

        List<AssetsSummaryResponse.AccountTypeBalance> byAccountType = byType.entrySet().stream()
                .map(entry -> {
                    String typeName = getTypeName(entry.getKey());
                    return AssetsSummaryResponse.AccountTypeBalance.builder()
                            .type(entry.getKey())
                            .typeName(typeName)
                            .balance(entry.getValue())
                            .build();
                })
                .collect(Collectors.toList());

        return AssetsSummaryResponse.builder()
                .totalAssets(totalAssets)
                .totalLiabilities(totalLiabilities)
                .netAssets(totalAssets.subtract(totalLiabilities))
                .accountCount(accounts.size())
                .byAccountType(byAccountType)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long bookId, Long userId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        String startDate = yearMonth.atDay(1).toString();
        String endDate = yearMonth.atEndOfMonth().toString();

        // 统计收支
        List<CategoryStatisticsResponse> expenseStats = getExpenseByCategory(bookId, startDate, endDate);
        List<CategoryStatisticsResponse> incomeStats = getIncomeByCategory(bookId, startDate, endDate);

        BigDecimal totalExpense = expenseStats.stream()
                .map(CategoryStatisticsResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = incomeStats.stream()
                .map(CategoryStatisticsResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 最高支出分类
        MonthlySummaryResponse.TopCategory topCategory = expenseStats.stream()
                .max(Comparator.comparing(CategoryStatisticsResponse::getAmount))
                .map(s -> MonthlySummaryResponse.TopCategory.builder()
                        .categoryId(s.getCategoryId())
                        .categoryName(s.getCategoryName())
                        .amount(s.getAmount())
                        .percentage(s.getPercentage())
                        .build())
                .orElse(null);

        // 预算执行情况
        MonthlySummaryResponse.BudgetExecution budgetExecution = getMonthlyBudgetExecution(bookId, year, month);

        // 日均支出
        long daysInMonth = yearMonth.lengthOfMonth();
        BigDecimal dailyAverage = totalExpense.divide(new BigDecimal(daysInMonth), 2, RoundingMode.HALF_UP);

        return MonthlySummaryResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .surplus(totalIncome.subtract(totalExpense))
                .expenseCount((long) expenseStats.stream()
                        .mapToLong(s -> s.getTransactionCount() != null ? s.getTransactionCount() : 0)
                        .sum())
                .incomeCount((long) incomeStats.stream()
                        .mapToLong(s -> s.getTransactionCount() != null ? s.getTransactionCount() : 0)
                        .sum())
                .dailyAverage(dailyAverage)
                .topCategory(topCategory)
                .budgetExecution(budgetExecution)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public YearlySummaryResponse getYearlySummary(Long bookId, Long userId, Integer year) {
        String startDate = year + "-01-01";
        String endDate = year + "-12-31";

        // 统计收支
        List<CategoryStatisticsResponse> expenseStats = getExpenseByCategory(bookId, startDate, endDate);
        List<CategoryStatisticsResponse> incomeStats = getIncomeByCategory(bookId, startDate, endDate);

        BigDecimal totalExpense = expenseStats.stream()
                .map(CategoryStatisticsResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome = incomeStats.stream()
                .map(CategoryStatisticsResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyAverageIncome = totalIncome.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyAverageExpense = totalExpense.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyAverageSurplus = monthlyAverageIncome.subtract(monthlyAverageExpense);

        // 最高支出分类
        YearlySummaryResponse.TopCategory topExpenseCategory = expenseStats.stream()
                .max(Comparator.comparing(CategoryStatisticsResponse::getAmount))
                .map(s -> YearlySummaryResponse.TopCategory.builder()
                        .categoryId(s.getCategoryId())
                        .categoryName(s.getCategoryName())
                        .amount(s.getAmount())
                        .percentage(s.getPercentage())
                        .build())
                .orElse(null);

        // 最高收入分类
        YearlySummaryResponse.TopCategory topIncomeCategory = incomeStats.stream()
                .max(Comparator.comparing(CategoryStatisticsResponse::getAmount))
                .map(s -> YearlySummaryResponse.TopCategory.builder()
                        .categoryId(s.getCategoryId())
                        .categoryName(s.getCategoryName())
                        .amount(s.getAmount())
                        .percentage(s.getPercentage())
                        .build())
                .orElse(null);

        return YearlySummaryResponse.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .surplus(totalIncome.subtract(totalExpense))
                .expenseCount((long) expenseStats.stream()
                        .mapToLong(s -> s.getTransactionCount() != null ? s.getTransactionCount() : 0)
                        .sum())
                .incomeCount((long) incomeStats.stream()
                        .mapToLong(s -> s.getTransactionCount() != null ? s.getTransactionCount() : 0)
                        .sum())
                .monthlyAverageIncome(monthlyAverageIncome)
                .monthlyAverageExpense(monthlyAverageExpense)
                .monthlyAverageSurplus(monthlyAverageSurplus)
                .topExpenseCategory(topExpenseCategory)
                .topIncomeCategory(topIncomeCategory)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetExecutionResponse getBudgetExecution(Long bookId, Long userId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 获取该月所有预算
        List<Budget> budgets = budgetRepository.findByBookId(bookId, null, null);

        List<BudgetExecutionResponse.BudgetItem> items = budgets.stream()
                .map(budget -> {
                    BigDecimal spentAmount = transactionRepository.sumExpensesByBookIdAndCategoryIdAndDateRange(
                            bookId,
                            budget.getCategoryId(),
                            startDate,
                            endDate
                    );
                    spentAmount = spentAmount != null ? spentAmount : BigDecimal.ZERO;

                    BigDecimal remaining = budget.getAmount().subtract(spentAmount);
                    BigDecimal progress = spentAmount.multiply(new BigDecimal("100"))
                            .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);

                    String status = "active";
                    if (progress.compareTo(new BigDecimal("100")) > 0) {
                        status = "overdue";
                    } else if (LocalDate.now().isAfter(budget.getEndDate())) {
                        status = "completed";
                    }

                    return BudgetExecutionResponse.BudgetItem.builder()
                            .budgetId(budget.getId())
                            .categoryId(budget.getCategoryId())
                            .categoryName(getCategoryName(budget.getCategoryId()))
                            .budgetAmount(budget.getAmount())
                            .spentAmount(spentAmount)
                            .remaining(remaining)
                            .progress(progress)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalBudget = items.stream()
                .map(BudgetExecutionResponse.BudgetItem::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = items.stream()
                .map(BudgetExecutionResponse.BudgetItem::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal overallProgress = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return BudgetExecutionResponse.builder()
                .year(year)
                .month(month)
                .budgets(items)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .overallProgress(overallProgress)
                .build();
    }

    /**
     * 获取分类统计
     */
    private List<CategoryStatisticsResponse> getCategoryStatistics(Long bookId, Integer type, String startDate, String endDate) {
        List<Transaction> transactions = queryTransactions(bookId, startDate, endDate);

        // 按分类分组
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .filter(tx -> tx.getType().equals(type))
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        // 计算总额
        BigDecimal totalAmount = byCategory.values().stream()
                .flatMap(List::stream)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建响应
        return byCategory.entrySet().stream()
                .map(entry -> {
                    Long categoryId = entry.getKey();
                    List<Transaction> txs = entry.getValue();

                    BigDecimal amount = txs.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(new BigDecimal("100")).divide(totalAmount, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    Category category = categoryRepository.selectById(categoryId);

                    return CategoryStatisticsResponse.builder()
                            .categoryId(categoryId)
                            .categoryName(category != null ? category.getName() : "未知分类")
                            .icon(category != null ? category.getIcon() : null)
                            .amount(amount)
                            .percentage(percentage)
                            .transactionCount((long) txs.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询交易记录
     */
    private List<Transaction> queryTransactions(Long bookId, String startDate, String endDate) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getDeleted, 0);

        if (startDate != null && endDate != null) {
            wrapper.ge(Transaction::getTransactionDate, LocalDate.parse(startDate))
                    .le(Transaction::getTransactionDate, LocalDate.parse(endDate));
        }

        return transactionRepository.selectList(wrapper);
    }

    /**
     * 获取周期字符串
     */
    private String getPeriod(LocalDate date, String trendType) {
        if (trendType == null) {
            trendType = "monthly";
        }

        switch (trendType) {
            case "daily":
                return date.toString();
            case "weekly":
                // 返回周一的日期
                return date.minusDays(date.getDayOfWeek().getValue() - 1).toString();
            case "yearly":
                return date.format(YEAR_FORMAT);
            case "monthly":
            default:
                return date.format(MONTH_FORMAT);
        }
    }

    /**
     * 获取账户类型名称
     */
    private String getTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        try {
            AccountType accountType = AccountType.fromCode(type);
            return accountType.getName();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
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
     * 获取月度预算执行
     */
    private MonthlySummaryResponse.BudgetExecution getMonthlyBudgetExecution(Long bookId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Budget> budgets = budgetRepository.findByBookId(bookId, "monthly", null);

        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = budgets.stream()
                .map(budget -> {
                    BigDecimal spent = transactionRepository.sumExpensesByBookIdAndCategoryIdAndDateRange(
                            bookId,
                            budget.getCategoryId(),
                            startDate,
                            endDate
                    );
                    return spent != null ? spent : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal progress = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MonthlySummaryResponse.BudgetExecution.builder()
                .totalBudget(totalBudget)
                .spentAmount(totalSpent)
                .progress(progress)
                .remaining(totalBudget.subtract(totalSpent))
                .build();
    }
}
