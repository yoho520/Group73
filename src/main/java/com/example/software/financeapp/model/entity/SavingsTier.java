package com.example.software.financeapp.model.entity;

import com.example.software.financeapp.model.enums.SavingsPriority;

import java.math.BigDecimal;

/**
 * 储蓄层级实体类
 */
public class SavingsTier {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private SavingsPriority priority;
    private BigDecimal allocationPercentage;
    private boolean active;

    public SavingsTier() {
    }

    public SavingsTier(Long id, Long userId, String name, String description,
                       SavingsPriority priority, BigDecimal allocationPercentage, boolean active) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.allocationPercentage = allocationPercentage;
        this.active = active;
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

    public SavingsPriority getPriority() {
        return priority;
    }

    public void setPriority(SavingsPriority priority) {
        this.priority = priority;
    }

    public BigDecimal getAllocationPercentage() {
        return allocationPercentage;
    }

    public void setAllocationPercentage(BigDecimal allocationPercentage) {
        this.allocationPercentage = allocationPercentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return name;
    }
}