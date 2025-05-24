package com.example.software.financeapp.model.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户名
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 密码(加密存储)
    @Column(nullable = false, length = 100)
    private String password;

    // 邮箱
    @Column(unique = true, length = 100)
    private String email;

    // 全名
    @Column(length = 100)
    private String fullName;

    // 手机号
    @Column(length = 20)
    private String phone;

    // 头像URL
    @Column(length = 255)
    private String avatarUrl;

    // 用户角色 (ROLE_USER, ROLE_ADMIN等)
    @Column(nullable = false, length = 20)
    private String role;

    // 账户是否已激活
    @Column(nullable = false)
    private boolean active;

    // 用户区域/地区设置
    @Column(length = 10)
    private String locale;

    // 用户所在城市
    @Column(length = 50)
    private String city;

    // 用户所在省份
    @Column(length = 50)
    private String province;

    // 用户创建的交易列表
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    // 用户自定义分类
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Category> categories;

    // 注册日期
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 最后登录时间
    @Column
    private LocalDateTime lastLoginAt;

    // 最后更新时间
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(this.role);
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * JPA 持久化前的处理
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // 设置默认值
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
        if (this.active == false) {
            this.active = true;
        }
    }

    /**
     * JPA 更新前的处理
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}