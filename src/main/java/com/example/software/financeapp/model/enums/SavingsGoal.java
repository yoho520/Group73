package com.example.software.financeapp.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 储蓄目标实体类
 */
public class SavingsGoal {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private SavingsTier tier;
    private boolean completed;

    public SavingsGoal() {
        this.currentAmount = BigDecimal.ZERO;
        this.completed = false;
    }

    public SavingsGoal(Long id, Long userId, String name, String description,
                       BigDecimal targetAmount, BigDecimal currentAmount,
                       LocalDate targetDate, SavingsTier tier, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.tier = tier;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
        // 自动更新完成状态
        if (this.targetAmount != null && currentAmount.compareTo(targetAmount) >= 0) {
            this.completed = true;
        }
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public SavingsTier getTier() {
        return tier;
    }

    public void setTier(SavingsTier tier) {
        this.tier = tier;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * 计算完成百分比
     * @return 完成百分比
     */
    public BigDecimal getCompletionPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算距离目标日期的天数
     * @return 剩余天数
     */
    public long getDaysRemaining() {
        if (targetDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
    }
}