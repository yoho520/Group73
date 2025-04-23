package com.example.software.financeapp.model.enums;

/**
 * 储蓄优先级枚举
 */
public enum SavingsPriority {
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");

    private final String displayName;

    SavingsPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}