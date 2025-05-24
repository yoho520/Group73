package com.example.software.financeapp.service.savings;

import com.example.software.financeapp.model.entity.SavingsGoal;
import com.example.software.financeapp.model.entity.SavingsTier;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.SavingsPriority;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能储蓄计划服务 - 提供分层储蓄建议和管理
 */
public class SavingsPlanService {

    private final ApiService apiService;

    public SavingsPlanService(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * 创建默认的储蓄层级
     * @param userId 用户ID
     * @return 默认储蓄层级列表
     */
    public List<SavingsTier> createDefaultTiers(Long userId) {
        List<SavingsTier> defaultTiers = new ArrayList<>();

        // 层级1: 紧急基金 (最高优先级)
        defaultTiers.add(new SavingsTier(
                null,
                userId,
                "紧急基金",
                "用于应对紧急情况的3-6个月生活费",
                SavingsPriority.HIGH,
                BigDecimal.valueOf(0.50), // 50%的储蓄分配
                true
        ));

        // 层级2: 短期目标
        defaultTiers.add(new SavingsTier(
                null,
                userId,
                "短期目标",
                "1-2年内的目标，如旅行或购买大件物品",
                SavingsPriority.MEDIUM,
                BigDecimal.valueOf(0.30), // 30%的储蓄分配
                true
        ));

        // 层级3: 长期投资
        defaultTiers.add(new SavingsTier(
                null,
                userId,
                "长期投资",
                "退休金或长期财务自由",
                SavingsPriority.LOW,
                BigDecimal.valueOf(0.20), // 20%的储蓄分配
                true
        ));

        return defaultTiers;
    }

    /**
     * 获取用户的储蓄层级
     * @param userId 用户ID
     * @return 储蓄层级列表
     */
    public List<SavingsTier> getUserSavingsTiers(Long userId) {
        try {
            // 调用API获取用户储蓄层级
            List<SavingsTier> tiers = apiService.getSavingsTiers(userId);

            // 如果用户没有储蓄层级，创建默认层级
            if (tiers == null || tiers.isEmpty()) {
                tiers = createDefaultTiers(userId);
                for (SavingsTier tier : tiers) {
                    apiService.createSavingsTier(tier);
                }
            }

            return tiers;
        } catch (Exception e) {
            System.err.println("获取储蓄层级失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 获取用户的储蓄目标
     * @param userId 用户ID
     * @return 储蓄目标列表
     */
    public List<SavingsGoal> getUserSavingsGoals(Long userId) {
        try {
            return apiService.getSavingsGoals(userId);
        } catch (Exception e) {
            System.err.println("获取储蓄目标失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 计算每月可储蓄金额
     * @param userId 用户ID
     * @param months 分析的月数
     * @return 建议的月储蓄金额
     */
    public BigDecimal calculateMonthlySavingsAmount(Long userId, int months) {
        try {
            // 获取用户最近几个月的交易记录
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(months);
            List<Transaction> transactions = apiService.getTransactions(userId, 0, 1000);

            // 过滤交易记录，只保留指定日期范围内的记录
            List<Transaction> filteredTransactions = transactions.stream()
                    .filter(t -> {
                        LocalDate transactionDate = t.getTransactionDate().toLocalDate();
                        return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // 计算月均收入和支出
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;

            for (Transaction transaction : filteredTransactions) {
                if (transaction.getType() == TransactionType.INCOME) {
                    totalIncome = totalIncome.add(transaction.getAmount());
                } else if (transaction.getType() == TransactionType.EXPENSE) {
                    totalExpense = totalExpense.add(transaction.getAmount());
                }
            }

            // 计算月均收入和支出
            BigDecimal monthlyIncome = totalIncome.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
            BigDecimal monthlyExpense = totalExpense.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

            // 计算每月可储蓄金额 (收入 - 支出)
            BigDecimal monthlySavings = monthlyIncome.subtract(monthlyExpense);

            // 如果计算结果为负数，建议最小储蓄额为收入的5%
            if (monthlySavings.compareTo(BigDecimal.ZERO) <= 0) {
                monthlySavings = monthlyIncome.multiply(BigDecimal.valueOf(0.05))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            return monthlySavings;

        } catch (Exception e) {
            System.err.println("计算每月储蓄金额失败: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * 生成智能储蓄计划
     * @param userId 用户ID
     * @return 储蓄计划
     */
    public SavingsPlan generateSavingsPlan(Long userId) {
        try {
            // 获取用户储蓄层级
            List<SavingsTier> tiers = getUserSavingsTiers(userId);

            // 计算建议的每月储蓄金额（基于过去6个月的数据）
            BigDecimal recommendedMonthlySavings = calculateMonthlySavingsAmount(userId, 6);

            // 计算每个层级的分配金额
            Map<SavingsTier, BigDecimal> tierAllocations = new HashMap<>();
            for (SavingsTier tier : tiers) {
                if (tier.isActive()) {
                    BigDecimal allocation = recommendedMonthlySavings.multiply(tier.getAllocationPercentage())
                            .setScale(2, RoundingMode.HALF_UP);
                    tierAllocations.put(tier, allocation);
                }
            }

            // 获取用户的储蓄目标
            List<SavingsGoal> goals = getUserSavingsGoals(userId);

            // 计算目标进度
            Map<SavingsGoal, SavingsGoalProgress> goalProgress = new HashMap<>();
            for (SavingsGoal goal : goals) {
                BigDecimal progressPercentage = goal.getCurrentAmount()
                        .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

                // 计算预计完成日期
                LocalDate expectedCompletionDate = calculateExpectedCompletionDate(
                        goal, tierAllocations.getOrDefault(goal.getTier(), BigDecimal.ZERO));

                goalProgress.put(goal, new SavingsGoalProgress(progressPercentage, expectedCompletionDate));
            }

            return new SavingsPlan(recommendedMonthlySavings, tierAllocations, goalProgress);

        } catch (Exception e) {
            System.err.println("生成储蓄计划失败: " + e.getMessage());
            return new SavingsPlan(BigDecimal.ZERO, new HashMap<>(), new HashMap<>());
        }
    }

    /**
     * 计算储蓄目标的预计完成日期
     * @param goal 储蓄目标
     * @param monthlyAllocation 每月分配金额
     * @return 预计完成日期
     */
    private LocalDate calculateExpectedCompletionDate(SavingsGoal goal, BigDecimal monthlyAllocation) {
        // 如果每月分配金额为0，返回遥远的未来日期
        if (monthlyAllocation.compareTo(BigDecimal.ZERO) <= 0) {
            return LocalDate.now().plusYears(10);
        }

        // 计算还需要储蓄的金额
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getCurrentAmount());

        // 如果已经达到目标，返回当前日期
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return LocalDate.now();
        }

        // 计算需要多少个月才能达到目标
        BigDecimal monthsNeeded = remainingAmount.divide(monthlyAllocation, 0, RoundingMode.CEILING);

        // 返回预计完成日期
        return LocalDate.now().plusMonths(monthsNeeded.longValue());
    }

    /**
     * 创建储蓄目标
     * @param goal 储蓄目标
     * @return 创建的储蓄目标
     */
    public SavingsGoal createSavingsGoal(SavingsGoal goal) {
        try {
            return apiService.createSavingsGoal(goal);
        } catch (Exception e) {
            System.err.println("创建储蓄目标失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 更新储蓄目标进度
     * @param goalId 目标ID
     * @param amount 更新金额
     * @return 更新后的储蓄目标
     */
    public SavingsGoal updateSavingsGoalProgress(Long goalId, BigDecimal amount) {
        try {
            SavingsGoal goal = apiService.getSavingsGoal(goalId);
            if (goal != null) {
                goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
                return apiService.updateSavingsGoal(goal);
            }
            return null;
        } catch (Exception e) {
            System.err.println("更新储蓄目标进度失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 储蓄计划类
     */
    public static class SavingsPlan {
        private final BigDecimal recommendedMonthlySavings;
        private final Map<SavingsTier, BigDecimal> tierAllocations;
        private final Map<SavingsGoal, SavingsGoalProgress> goalProgress;

        public SavingsPlan(BigDecimal recommendedMonthlySavings,
                           Map<SavingsTier, BigDecimal> tierAllocations,
                           Map<SavingsGoal, SavingsGoalProgress> goalProgress) {
            this.recommendedMonthlySavings = recommendedMonthlySavings;
            this.tierAllocations = tierAllocations;
            this.goalProgress = goalProgress;
        }

        public BigDecimal getRecommendedMonthlySavings() {
            return recommendedMonthlySavings;
        }

        public Map<SavingsTier, BigDecimal> getTierAllocations() {
            return tierAllocations;
        }

        public Map<SavingsGoal, SavingsGoalProgress> getGoalProgress() {
            return goalProgress;
        }
    }

    /**
     * 储蓄目标进度类
     */
    public static class SavingsGoalProgress {
        private final BigDecimal progressPercentage;
        private final LocalDate expectedCompletionDate;

        public SavingsGoalProgress(BigDecimal progressPercentage, LocalDate expectedCompletionDate) {
            this.progressPercentage = progressPercentage;
            this.expectedCompletionDate = expectedCompletionDate;
        }

        public BigDecimal getProgressPercentage() {
            return progressPercentage;
        }

        public LocalDate getExpectedCompletionDate() {
            return expectedCompletionDate;
        }

        public long getMonthsToCompletion() {
            return ChronoUnit.MONTHS.between(LocalDate.now(), expectedCompletionDate);
        }
    }
}