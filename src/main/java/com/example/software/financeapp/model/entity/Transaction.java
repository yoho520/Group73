package com.example.software.financeapp.model.entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.example.software.financeapp.model.enums.TransactionType;
/**
 * 交易实体类
 * 用于存储用户的收入和支出交易记录
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 交易金额
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // 交易类型：收入或支出
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // 交易描述
    @Column(length = 255)
    private String description;

    // 交易日期时间
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    // 交易所属类别
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // 交易所属用户
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 交易来源（手动输入、支付宝、微信、银行等）
    @Column(length = 50)
    private String source;

    // 商家名称
    @Column(length = 100)
    private String merchant;

    // 导入时的原始数据（JSON格式，用于溯源）
    @Column(columnDefinition = "TEXT")
    private String rawData;

    // AI分类的置信度分数
    @Column(precision = 5, scale = 2)
    private BigDecimal aiConfidenceScore;

    // 是否已被用户确认/修正分类
    @Column
    private boolean categoryConfirmed;

    // 创建时间
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 最后更新时间
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 实现一些业务逻辑方法

    /**
     * 判断是否为支出
     */
    public boolean isExpense() {
        return this.type == TransactionType.EXPENSE;
    }

    /**
     * 判断是否为收入
     */
    public boolean isIncome() {
        return this.type == TransactionType.INCOME;
    }

    /**
     * 用户确认或修正类别
     */
    public void confirmCategory(Category category) {
        this.category = category;
        this.categoryConfirmed = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 持久化前的处理
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 更新前的处理
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
