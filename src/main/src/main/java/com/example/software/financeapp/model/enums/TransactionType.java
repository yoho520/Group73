package com.example.software.financeapp.model.enums;

/**
 * 交易类型枚举
 * 用于区分交易是收入还是支出
 */
public enum TransactionType {
    /**
     * 收入类型交易
     */
    INCOME("收入"),

    /**
     * 支出类型交易
     */
    EXPENSE("支出"),

    /**
     * 转账类型交易
     */
    TRANSFER("转账");

    private final String displayName;

    TransactionType(String displayName) {
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
    public static TransactionType fromDisplayName(String displayName) {
        for (TransactionType type : TransactionType.values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的交易类型显示名称: " + displayName);
    }
}