package com.example.software.financeapp.service;

import com.example.software.financeapp.model.entity.User;
import java.io.IOException;

/**
 * 用户服务类 - 处理用户相关逻辑
 * 注意：这是简化版，使用了模拟数据
 */
public class UserService {

    private final ApiService apiService;

    // 添加接收ApiService的构造函数
    public UserService(ApiService apiService) {
        this.apiService = apiService;
    }
    // 登录方法
    public User login(String username, String password) throws IOException {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        // 在实际应用中，这里会验证密码，目前仅做简单模拟
        if ("admin".equals(username) && "admin".equals(password)) {
            return User.builder()
                    .id(1L)
                    .username(username)
                    .fullName("管理员")
                    .email("admin@example.com")
                    .role("ROLE_ADMIN")
                    .active(true)
                    .build();
        } else if ("user".equals(username) && "password".equals(password)) {
            return User.builder()
                    .id(2L)
                    .username(username)
                    .fullName("普通用户")
                    .email("user@example.com")
                    .role("ROLE_USER")
                    .active(true)
                    .build();
        }

        throw new IOException("用户名或密码不正确");
    }

    // 检查用户是否登录
    public boolean isLoggedIn(User user) {
        return user != null;
    }

    // 检查用户是否有特定权限
    public boolean hasPermission(User user, String permission) {
        if (user == null) return false;

        if ("ROLE_ADMIN".equals(user.getRole())) {
            // 管理员拥有所有权限
            return true;
        }

        // 在实际应用中，这里会检查具体权限
        return "ROLE_USER".equals(user.getRole()) && "basic_access".equals(permission);
    }
}