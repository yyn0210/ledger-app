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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    @Override
    public CategoryStatisticsSummaryResponse getExpenseByCategory(Long bookId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("支出统计（按分类），bookId: {}, startDate: {}, endDate: {}", bookId, startDate, endDate);

        return getCategoryStatistics(bookId, startDate, endDate, 2); // 2 = 支出
    }

    @Override
    public CategoryStatisticsSummaryResponse getIncomeByCategory(Long bookId, Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("收入统计（按分类），bookId: {}, startDate: {}, endDate: {}", bookId, startDate, endDate);

        return getCategoryStatistics(bookId, startDate, endDate, 1); // 1 = 收入
    }

    @Override
    public TrendSummaryResponse getTrend(Long bookId, Long userId, String type, LocalDate startDate, LocalDate endDate) {
        log.info("收支趋势分析，bookId: {}, type: {}, startDate: {}, endDate: {}", bookId, type, startDate, endDate);

        TrendSummaryResponse response = new TrendSummaryResponse();
        List<TrendResponse> trend = new ArrayList<>();

        // 按周期分组统计
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate);

        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        // 按周期分组
        Map<String, List<Transaction>> grouped = groupByPeriod(transactions, type);

        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
            TrendResponse trendResp = new TrendResponse();
            trendResp.setPeriod(entry.getKey());

            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;

            for (Transaction tx : entry.getValue()) {
                if (tx.getType() == 1) { // 收入
                    income = income.add(tx.getAmount());
                } else if (tx.getType() == 2) { // 支出
                    expense = expense.add(tx.getAmount());
                }
            }

            trendResp.setIncome(income);
            trendResp.setExpense(expense);
            trendResp.setSurplus(income.subtract(expense));
            trend.add(trendResp);
        }

        // 按周期排序
        trend.sort(Comparator.comparing(TrendResponse::getPeriod));
        response.setTrend(trend);

        return response;
    }

    @Override
    public RankingSummaryResponse getRanking(Long bookId, Long userId, String type, Integer limit, LocalDate startDate, LocalDate endDate) {
        log.info("收支排行榜，bookId: {}, type: {}, limit: {}", bookId, type, limit);

        RankingSummaryResponse response = new RankingSummaryResponse();
        response.setType(type);

        int transactionType = "expense".equals(type) ? 2 : 1;

        // 查询按分类汇总
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getType, transactionType)
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate)
                .groupBy(Transaction::getCategoryId);

        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        // 按分类汇总
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<RankingResponse> ranking = new ArrayList<>();
        BigDecimal total = byCategory.values().stream()
                .flatMap(List::stream)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int rank = 1;
        for (Map.Entry<Long, List<Transaction>> entry : byCategory.entrySet()) {
            RankingResponse item = new RankingResponse();
            item.setRank(rank++);
            item.setCategoryId(entry.getKey());

            BigDecimal amount = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            item.setAmount(amount);
            item.setTransactionCount(entry.getValue().size());
            item.setPercentage(total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            // 获取分类名称
            if (entry.getKey() != null) {
                Category category = categoryRepository.selectById(entry.getKey());
                if (category != null) {
                    item.setCategoryName(category.getName());
                }
            }

            ranking.add(item);
        }

        // 按金额降序排序并取 TOP N
        ranking.sort(Comparator.comparing(RankingResponse::getAmount).reversed());
        if (limit != null && limit > 0) {
            ranking = ranking.stream().limit(limit).collect(Collectors.toList());
        }

        // 重新设置排名
        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setRank(i + 1);
        }

        response.setRanking(ranking);
        return response;
    }

    @Override
    public AssetsSummaryResponse getAssetsSummary(Long bookId, Long userId) {
        log.info("资产汇总统计，bookId: {}", bookId);

        AssetsSummaryResponse response = new AssetsSummaryResponse();

        List<Account> accounts = accountRepository.selectByBookIdAndUserId(bookId, userId);

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        Map<Integer, BigDecimal> byType = new HashMap<>();

        for (Account account : accounts) {
            BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;

            // 信用卡等负债账户
            if (account.getType() != null && account.getType() == 3) { // 3 = 信用卡
                totalLiabilities = totalLiabilities.add(balance.abs());
            } else {
                totalAssets = totalAssets.add(balance);
            }

            byType.merge(account.getType(), balance, BigDecimal::add);
        }

        response.setTotalAssets(totalAssets);
        response.setTotalLiabilities(totalLiabilities);
        response.setNetAssets(totalAssets.subtract(totalLiabilities));
        response.setAccountCount(accounts.size());

        // 按账户类型分组
        List<AssetsSummaryResponse.AccountTypeBalance> byAccountType = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : byType.entrySet()) {
            AssetsSummaryResponse.AccountTypeBalance item = new AssetsSummaryResponse.AccountTypeBalance();
            item.setType(entry.getKey());
            item.setBalance(entry.getValue());

            // 获取类型名称
            try {
                AccountType accountType = AccountType.fromCode(entry.getKey());
                item.setTypeName(accountType.getName());
            } catch (IllegalArgumentException e) {
                item.setTypeName("未知");
            }

            byAccountType.add(item);
        }

        response.setByAccountType(byAccountType);
        return response;
    }

    @Override
    public MonthlySummaryResponse getMonthlySummary(Long bookId, Long userId, Integer year, Integer month) {
        log.info("月度收支概览，bookId: {}, year: {}, month: {}", bookId, year, month);

        MonthlySummaryResponse response = new MonthlySummaryResponse();
        response.setYear(year);
        response.setMonth(month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 查询该月交易
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate);

        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int expenseCount = 0;
        int incomeCount = 0;
        Map<Long, BigDecimal> byCategory = new HashMap<>();

        for (Transaction tx : transactions) {
            if (tx.getType() == 1) { // 收入
                totalIncome = totalIncome.add(tx.getAmount());
                incomeCount++;
            } else if (tx.getType() == 2) { // 支出
                totalExpense = totalExpense.add(tx.getAmount());
                expenseCount++;
                byCategory.merge(tx.getCategoryId(), tx.getAmount(), BigDecimal::add);
            }
        }

        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setSurplus(totalIncome.subtract(totalExpense));
        response.setExpenseCount(expenseCount);
        response.setIncomeCount(incomeCount);

        // 计算日均支出
        int days = endDate.getDayOfMonth();
        response.setDailyAverage(totalExpense.divide(new BigDecimal(days), 2, RoundingMode.HALF_UP));

        // 找出最高支出分类
        if (!byCategory.isEmpty()) {
            Map.Entry<Long, BigDecimal> top = byCategory.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue))
                    .orElse(null);

            if (top != null) {
                MonthlySummaryResponse.TopCategory topCategory = new MonthlySummaryResponse.TopCategory();
                topCategory.setCategoryId(top.getKey());
                topCategory.setAmount(top.getValue());
                topCategory.setPercentage(totalExpense.compareTo(BigDecimal.ZERO) > 0
                        ? top.getValue().multiply(new BigDecimal("100")).divide(totalExpense, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

                if (top.getKey() != null) {
                    Category category = categoryRepository.selectById(top.getKey());
                    if (category != null) {
                        topCategory.setCategoryName(category.getName());
                    }
                }

                response.setTopCategory(topCategory);
            }
        }

        // 预算执行情况
        MonthlySummaryResponse.BudgetExecution budgetExec = new MonthlySummaryResponse.BudgetExecution();
        List<Budget> budgets = budgetRepository.selectByBookIdAndUserId(bookId, userId);
        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        budgetExec.setTotalBudget(totalBudget);
        budgetExec.setSpentAmount(totalExpense);
        budgetExec.setProgress(totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalExpense.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        budgetExec.setRemaining(totalBudget.subtract(totalExpense));
        response.setBudgetExecution(budgetExec);

        return response;
    }

    @Override
    public YearlySummaryResponse getYearlySummary(Long bookId, Long userId, Integer year) {
        log.info("年度统计概览，bookId: {}, year: {}", bookId, year);

        YearlySummaryResponse response = new YearlySummaryResponse();
        response.setYear(year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // 查询该年交易
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate);

        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int expenseCount = 0;
        int incomeCount = 0;
        Map<Long, BigDecimal> expenseByCategory = new HashMap<>();
        Map<Long, BigDecimal> incomeByCategory = new HashMap<>();

        for (Transaction tx : transactions) {
            if (tx.getType() == 1) { // 收入
                totalIncome = totalIncome.add(tx.getAmount());
                incomeCount++;
                incomeByCategory.merge(tx.getCategoryId(), tx.getAmount(), BigDecimal::add);
            } else if (tx.getType() == 2) { // 支出
                totalExpense = totalExpense.add(tx.getAmount());
                expenseCount++;
                expenseByCategory.merge(tx.getCategoryId(), tx.getAmount(), BigDecimal::add);
            }
        }

        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setSurplus(totalIncome.subtract(totalExpense));
        response.setExpenseCount(expenseCount);
        response.setIncomeCount(incomeCount);

        // 月均统计
        YearlySummaryResponse.MonthlyAverage monthlyAvg = new YearlySummaryResponse.MonthlyAverage();
        monthlyAvg.setIncome(totalIncome.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP));
        monthlyAvg.setExpense(totalExpense.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP));
        monthlyAvg.setSurplus(monthlyAvg.getIncome().subtract(monthlyAvg.getExpense()));
        response.setMonthlyAverage(monthlyAvg);

        // 最高支出分类
        if (!expenseByCategory.isEmpty()) {
            response.setTopExpenseCategory(findTopCategory(expenseByCategory, totalExpense));
        }

        // 最高收入分类
        if (!incomeByCategory.isEmpty()) {
            response.setTopIncomeCategory(findTopCategory(incomeByCategory, totalIncome));
        }

        return response;
    }

    @Override
    public BudgetExecutionResponse getBudgetExecution(Long bookId, Long userId, Integer year, Integer month) {
        log.info("预算执行对比，bookId: {}, year: {}, month: {}", bookId, year, month);

        BudgetExecutionResponse response = new BudgetExecutionResponse();
        response.setYear(year);
        response.setMonth(month);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        // 获取该月所有预算
        List<Budget> budgets = budgetRepository.selectByBookIdAndUserId(bookId, userId);

        List<BudgetExecutionResponse.BudgetExecutionItem> items = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            BudgetExecutionResponse.BudgetExecutionItem item = new BudgetExecutionResponse.BudgetExecutionItem();
            item.setBudgetId(budget.getId());
            item.setCategoryId(budget.getCategoryId());
            item.setBudgetAmount(budget.getAmount());

            // 统计实际支出
            BigDecimal spentAmount = calculateSpentAmount(bookId, budget.getCategoryId(), startDate, endDate);
            item.setSpentAmount(spentAmount);
            item.setRemaining(budget.getAmount().subtract(spentAmount));
            item.setProgress(budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spentAmount.multiply(new BigDecimal("100")).divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            // 确定状态
            if (item.getProgress().compareTo(new BigDecimal("100")) > 0) {
                item.setStatus("overdue");
            } else if (LocalDate.now().isAfter(budget.getEndDate())) {
                item.setStatus("completed");
            } else {
                item.setStatus("active");
            }

            // 获取分类名称
            if (budget.getCategoryId() != null) {
                Category category = categoryRepository.selectById(budget.getCategoryId());
                if (category != null) {
                    item.setCategoryName(category.getName());
                }
            }

            items.add(item);
            totalBudget = totalBudget.add(budget.getAmount());
            totalSpent = totalSpent.add(spentAmount);
        }

        response.setBudgets(items);
        response.setTotalBudget(totalBudget);
        response.setTotalSpent(totalSpent);
        response.setOverallProgress(totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.multiply(new BigDecimal("100")).divide(totalBudget, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        return response;
    }

    /**
     * 获取分类统计
     */
    private CategoryStatisticsSummaryResponse getCategoryStatistics(Long bookId, LocalDate startDate, LocalDate endDate, Integer type) {
        CategoryStatisticsSummaryResponse response = new CategoryStatisticsSummaryResponse();

        // 查询交易
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getType, type)
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate);

        List<Transaction> transactions = transactionRepository.selectList(wrapper);

        // 按分类汇总
        Map<Long, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<CategoryStatisticsResponse> categories = new ArrayList<>();
        BigDecimal total = byCategory.values().stream()
                .flatMap(List::stream)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map.Entry<Long, List<Transaction>> entry : byCategory.entrySet()) {
            CategoryStatisticsResponse item = new CategoryStatisticsResponse();
            item.setCategoryId(entry.getKey());

            BigDecimal amount = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            item.setAmount(amount);
            item.setTransactionCount(entry.getValue().size());
            item.setPercentage(total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

            // 获取分类信息
            if (entry.getKey() != null) {
                Category category = categoryRepository.selectById(entry.getKey());
                if (category != null) {
                    item.setCategoryName(category.getName());
                    item.setCategoryIcon(category.getIcon());
                }
            }

            categories.add(item);
        }

        // 按金额降序排序
        categories.sort(Comparator.comparing(CategoryStatisticsResponse::getAmount).reversed());
        response.setCategories(categories);
        response.setTotalAmount(total);

        return response;
    }

    /**
     * 按周期分组
     */
    private Map<String, List<Transaction>> groupByPeriod(List<Transaction> transactions, String type) {
        DateTimeFormatter formatter;
        switch (type) {
            case "daily":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "weekly":
                formatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
                break;
            case "yearly":
                formatter = DateTimeFormatter.ofPattern("yyyy");
                break;
            case "monthly":
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
        }

        return transactions.stream()
                .collect(Collectors.groupingBy(tx -> tx.getTransactionDate().format(formatter)));
    }

    /**
     * 计算指定分类的支出
     */
    private BigDecimal calculateSpentAmount(Long bookId, Long categoryId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getBookId, bookId)
                .eq(Transaction::getType, 2) // 支出
                .eq(Transaction::getDeleted, 0)
                .ge(Transaction::getTransactionDate, startDate)
                .le(Transaction::getTransactionDate, endDate);

        if (categoryId != null) {
            wrapper.eq(Transaction::getCategoryId, categoryId);
        }

        List<Transaction> transactions = transactionRepository.selectList(wrapper);
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 找出最高分类
     */
    private YearlySummaryResponse.TopCategory findTopCategory(Map<Long, BigDecimal> byCategory, BigDecimal total) {
        Map.Entry<Long, BigDecimal> top = byCategory.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null);

        if (top == null) {
            return null;
        }

        YearlySummaryResponse.TopCategory result = new YearlySummaryResponse.TopCategory();
        result.setCategoryId(top.getKey());
        result.setAmount(top.getValue());
        result.setPercentage(total.compareTo(BigDecimal.ZERO) > 0
                ? top.getValue().multiply(new BigDecimal("100")).divide(total, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);

        if (top.getKey() != null) {
            Category category = categoryRepository.selectById(top.getKey());
            if (category != null) {
                result.setCategoryName(category.getName());
            }
        }

        return result;
    }
}
