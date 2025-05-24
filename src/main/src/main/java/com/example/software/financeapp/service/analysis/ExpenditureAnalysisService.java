package com.example.software.financeapp.service.analysis;

import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支出分析服务 - 提供支出趋势分析和预算建议
 */
public class ExpenditureAnalysisService {

    /**
     * 计算按月分组的支出趋势
     * @param transactions 交易记录列表
     * @param monthsCount 要分析的月份数量
     * @return 按月分组的支出趋势数据
     */
    public Map<YearMonth, BigDecimal> calculateMonthlyTrend(List<Transaction> transactions, int monthsCount) {
        // 确定分析的开始月份
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(monthsCount - 1);

        // 初始化结果映射，确保每个月都有记录
        Map<YearMonth, BigDecimal> monthlyTotals = new LinkedHashMap<>();
        for (int i = 0; i < monthsCount; i++) {
            monthlyTotals.put(startMonth.plusMonths(i), BigDecimal.ZERO);
        }

        // 过滤出支出交易并按月份分组计算总额
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    YearMonth transactionMonth = YearMonth.from(t.getTransactionDate());
                    return !transactionMonth.isBefore(startMonth) && !transactionMonth.isAfter(currentMonth);
                })
                .forEach(t -> {
                    YearMonth month = YearMonth.from(t.getTransactionDate());
                    monthlyTotals.put(month, monthlyTotals.getOrDefault(month, BigDecimal.ZERO).add(t.getAmount()));
                });

        return monthlyTotals;
    }

    /**
     * 计算按类别分组的支出分布
     * @param transactions 交易记录列表
     * @param startDate 分析的开始日期
     * @param endDate 分析的结束日期
     * @return 按类别分组的支出分布数据
     */
    public Map<Category, BigDecimal> calculateCategoryDistribution(List<Transaction> transactions,
                                                                   LocalDate startDate,
                                                                   LocalDate endDate) {
        Map<Category, BigDecimal> categoryTotals = new HashMap<>();

        transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    LocalDate transactionDate = t.getTransactionDate().toLocalDate();
                    return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                })
                .forEach(t -> {
                    Category category = t.getCategory();
                    if (category != null) {
                        categoryTotals.put(category,
                                categoryTotals.getOrDefault(category, BigDecimal.ZERO).add(t.getAmount()));
                    }
                });

        return categoryTotals;
    }

    /**
     * 生成预算建议
     * @param transactions 历史交易记录
     * @param monthsToAnalyze 分析的历史月份数
     * @return 预算建议与分析结果
     */
    public BudgetRecommendation generateBudgetRecommendation(List<Transaction> transactions, int monthsToAnalyze) {
        // 分析最近几个月的平均支出
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(monthsToAnalyze);

        // 过滤相关时间范围内的支出交易
        List<Transaction> relevantTransactions = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    YearMonth transactionMonth = YearMonth.from(t.getTransactionDate());
                    return !transactionMonth.isBefore(startMonth) && transactionMonth.isBefore(currentMonth);
                })
                .collect(Collectors.toList());

        // 计算各类别的平均月支出
        Map<Category, List<Transaction>> transactionsByCategory = relevantTransactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(Transaction::getCategory));

        Map<Category, BigDecimal> averageMonthlyByCategory = new HashMap<>();
        for (Map.Entry<Category, List<Transaction>> entry : transactionsByCategory.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal average = total.divide(BigDecimal.valueOf(monthsToAnalyze), 2, BigDecimal.ROUND_HALF_UP);
            averageMonthlyByCategory.put(entry.getKey(), average);
        }

        // 计算总平均月支出
        BigDecimal totalMonthlyAverage = averageMonthlyByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 识别增长最快的类别
        Map<Category, BigDecimal> growthRates = calculateCategoryGrowthRates(relevantTransactions, monthsToAnalyze);

        // 创建预算建议
        return new BudgetRecommendation(
                totalMonthlyAverage,
                averageMonthlyByCategory,
                findTopGrowthCategories(growthRates, 3),
                identifyPotentialSavings(averageMonthlyByCategory, growthRates)
        );
    }

    /**
     * 计算各类别的支出增长率
     */
    private Map<Category, BigDecimal> calculateCategoryGrowthRates(List<Transaction> transactions, int monthsToAnalyze) {
        // 按月和类别分组计算支出
        Map<YearMonth, Map<Category, BigDecimal>> monthlyExpensesByCategory = new HashMap<>();

        for (int i = 0; i < monthsToAnalyze; i++) {
            YearMonth month = YearMonth.now().minusMonths(i + 1);
            monthlyExpensesByCategory.put(month, new HashMap<>());
        }

        transactions.forEach(t -> {
            YearMonth month = YearMonth.from(t.getTransactionDate());
            if (monthlyExpensesByCategory.containsKey(month)) {
                Category category = t.getCategory();
                if (category != null) {
                    Map<Category, BigDecimal> categoryMap = monthlyExpensesByCategory.get(month);
                    categoryMap.put(category, categoryMap.getOrDefault(category, BigDecimal.ZERO).add(t.getAmount()));
                }
            }
        });

        // 计算增长率
        Map<Category, BigDecimal> growthRates = new HashMap<>();

        // 获取所有类别
        Set<Category> allCategories = new HashSet<>();
        monthlyExpensesByCategory.values().forEach(map -> allCategories.addAll(map.keySet()));

        // 计算每个类别的增长趋势
        for (Category category : allCategories) {
            List<BigDecimal> monthlyAmounts = new ArrayList<>();

            for (int i = 0; i < monthsToAnalyze; i++) {
                YearMonth month = YearMonth.now().minusMonths(i + 1);
                Map<Category, BigDecimal> monthData = monthlyExpensesByCategory.get(month);
                monthlyAmounts.add(monthData.getOrDefault(category, BigDecimal.ZERO));
            }

            // 简化的线性增长率计算 (最新月份/第一个月份 - 1)
            if (monthlyAmounts.size() >= 2 && monthlyAmounts.get(0).compareTo(BigDecimal.ZERO) > 0
                    && monthlyAmounts.get(monthlyAmounts.size() - 1).compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal recent = monthlyAmounts.get(0);
                BigDecimal oldest = monthlyAmounts.get(monthlyAmounts.size() - 1);
                BigDecimal growthRate = recent.divide(oldest, 4, BigDecimal.ROUND_HALF_UP)
                        .subtract(BigDecimal.ONE);
                growthRates.put(category, growthRate);
            }
        }

        return growthRates;
    }

    /**
     * 找出增长最快的前N个类别
     */
    private List<CategoryGrowthInfo> findTopGrowthCategories(Map<Category, BigDecimal> growthRates, int count) {
        return growthRates.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .limit(count)
                .map(entry -> new CategoryGrowthInfo(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 识别潜在的节省机会
     */
    private List<SavingSuggestion> identifyPotentialSavings(
            Map<Category, BigDecimal> averageMonthlyByCategory,
            Map<Category, BigDecimal> growthRates) {

        List<SavingSuggestion> suggestions = new ArrayList<>();

        // 找出增长快且金额大的类别作为节省建议
        for (Map.Entry<Category, BigDecimal> entry : averageMonthlyByCategory.entrySet()) {
            Category category = entry.getKey();
            BigDecimal monthlyAverage = entry.getValue();
            BigDecimal growthRate = growthRates.getOrDefault(category, BigDecimal.ZERO);

            // 如果月平均支出较高且增长率为正
            if (monthlyAverage.compareTo(BigDecimal.valueOf(200)) > 0 && growthRate.compareTo(BigDecimal.ZERO) > 0) {
                // 建议节省10-20%
                BigDecimal savingRate = BigDecimal.valueOf(0.1);
                if (growthRate.compareTo(BigDecimal.valueOf(0.2)) > 0) {
                    savingRate = BigDecimal.valueOf(0.2);
                }

                BigDecimal suggestedSaving = monthlyAverage.multiply(savingRate)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                suggestions.add(new SavingSuggestion(category, suggestedSaving,
                        "该类别支出较高且增长迅速，建议控制增长"));
            }
        }

        return suggestions;
    }

    /**
     * 预算建议结果类
     */
    public static class BudgetRecommendation {
        private final BigDecimal totalMonthlyAverage;
        private final Map<Category, BigDecimal> categoryAverages;
        private final List<CategoryGrowthInfo> topGrowthCategories;
        private final List<SavingSuggestion> savingSuggestions;

        public BudgetRecommendation(BigDecimal totalMonthlyAverage,
                                    Map<Category, BigDecimal> categoryAverages,
                                    List<CategoryGrowthInfo> topGrowthCategories,
                                    List<SavingSuggestion> savingSuggestions) {
            this.totalMonthlyAverage = totalMonthlyAverage;
            this.categoryAverages = categoryAverages;
            this.topGrowthCategories = topGrowthCategories;
            this.savingSuggestions = savingSuggestions;
        }

        public BigDecimal getTotalMonthlyAverage() {
            return totalMonthlyAverage;
        }

        public Map<Category, BigDecimal> getCategoryAverages() {
            return categoryAverages;
        }

        public List<CategoryGrowthInfo> getTopGrowthCategories() {
            return topGrowthCategories;
        }

        public List<SavingSuggestion> getSavingSuggestions() {
            return savingSuggestions;
        }

        // 生成推荐预算(简单地根据历史平均值计算)
        public Map<Category, BigDecimal> generateRecommendedBudget() {
            Map<Category, BigDecimal> recommendedBudget = new HashMap<>();

            for (Map.Entry<Category, BigDecimal> entry : categoryAverages.entrySet()) {
                // 为增长较快的类别控制预算
                BigDecimal adjustmentFactor = BigDecimal.ONE;
                for (CategoryGrowthInfo growthInfo : topGrowthCategories) {
                    if (growthInfo.getCategory().equals(entry.getKey())) {
                        // 对增长较快的类别预算上调较少
                        adjustmentFactor = BigDecimal.valueOf(1.05); // 只增加5%
                        break;
                    }
                }

                // 默认预算为历史平均值的1.1倍(上浮10%)
                BigDecimal recommendedAmount = entry.getValue().multiply(BigDecimal.valueOf(1.1).multiply(adjustmentFactor))
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                recommendedBudget.put(entry.getKey(), recommendedAmount);
            }

            return recommendedBudget;
        }
    }

    /**
     * 类别增长信息
     */
    public static class CategoryGrowthInfo {
        private final Category category;
        private final BigDecimal growthRate;

        public CategoryGrowthInfo(Category category, BigDecimal growthRate) {
            this.category = category;
            this.growthRate = growthRate;
        }

        public Category getCategory() {
            return category;
        }

        public BigDecimal getGrowthRate() {
            return growthRate;
        }

        public String getGrowthRateFormatted() {
            return growthRate.multiply(BigDecimal.valueOf(100))
                    .setScale(1, BigDecimal.ROUND_HALF_UP) + "%";
        }
    }

    /**
     * 节省建议
     */
    public static class SavingSuggestion {
        private final Category category;
        private final BigDecimal suggestedSaving;
        private final String reason;

        public SavingSuggestion(Category category, BigDecimal suggestedSaving, String reason) {
            this.category = category;
            this.suggestedSaving = suggestedSaving;
            this.reason = reason;
        }

        public Category getCategory() {
            return category;
        }

        public BigDecimal getSuggestedSaving() {
            return suggestedSaving;
        }

        public String getReason() {
            return reason;
        }
    }
}