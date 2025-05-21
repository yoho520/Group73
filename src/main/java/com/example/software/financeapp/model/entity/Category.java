package com.example.software.financeapp.model.entity;

import com.example.software.financeapp.model.enums.CategoryType;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Column(length = 30)
    private String icon;

    @Column(length = 20)
    private String color;

    @Column
    private boolean systemDefault;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(length = 255)
    private String keywords;

    @Column
    private boolean seasonal;

    @Column(length = 100)
    private String seasonalMonths;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // null表示系统预设分类

    // 构造函数
    public Category() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 创建Builder模式静态内部类
    public static class Builder {
        private Category category = new Category();

        public Builder id(Long id) {
            category.id = id;
            return this;
        }

        public Builder name(String name) {
            category.name = name;
            return this;
        }

        public Builder description(String description) {
            category.description = description;
            return this;
        }

        public Builder type(CategoryType type) {
            category.type = type;
            return this;
        }

        public Builder icon(String icon) {
            category.icon = icon;
            return this;
        }

        public Builder color(String color) {
            category.color = color;
            return this;
        }

        public Builder systemDefault(boolean systemDefault) {
            category.systemDefault = systemDefault;
            return this;
        }

        public Builder parent(Category parent) {
            category.parent = parent;
            return this;
        }

        public Builder keywords(String keywords) {
            category.keywords = keywords;
            return this;
        }

        public Builder seasonal(boolean seasonal) {
            category.seasonal = seasonal;
            return this;
        }

        public Builder seasonalMonths(String seasonalMonths) {
            category.seasonalMonths = seasonalMonths;
            return this;
        }

        public Builder user(User user) {
            category.user = user;
            return this;
        }

        public Category build() {
            return category;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isSystemDefault() { return systemDefault; }
    public void setSystemDefault(boolean systemDefault) { this.systemDefault = systemDefault; }

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public boolean isSeasonal() { return seasonal; }
    public void setSeasonal(boolean seasonal) { this.seasonal = seasonal; }

    public String getSeasonalMonths() { return seasonalMonths; }
    public void setSeasonalMonths(String seasonalMonths) { this.seasonalMonths = seasonalMonths; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // 实用方法
    public boolean isIncome() {
        return this.type == CategoryType.INCOME;
    }

    public boolean isExpense() {
        return this.type == CategoryType.EXPENSE;
    }

    public boolean isMainCategory() {
        return this.parent == null;
    }

    public boolean isSeasonalForMonth(int month) {
        if (!this.seasonal || this.seasonalMonths == null) {
            return false;
        }

        return this.seasonalMonths.contains("\"" + month + "\"");
    }

    // 添加JPA实体生命周期方法
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}