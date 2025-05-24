package com.example.software.financeapp.model.enums;

/**
 * 分类类型枚举
 * 用于区分分类是收入类型还是支出类型
 */
public enum CategoryType {
    /**
     * 收入类分类
     */
    INCOME("收入"),

    /**
     * 支出类分类
     */
    EXPENSE("支出"),

    /**
     * 通用类分类（可用于收入和支出）
     */
    GENERAL("通用");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取用于显示的名称
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * 从显示名称获取枚举值
     */
    public static CategoryType fromDisplayName(String displayName) {
        for (CategoryType type : CategoryType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的分类类型显示名称: " + displayName);
    }
}