package com.example.software.financeapp.service;

import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * API服务类 - 用于与后端API通信
 * 注意：这是简化版，使用了模拟数据而非实际API调用
 */
public class ApiService {

    private final String baseUrl;
    private String authToken;

    public ApiService(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // 设置认证令牌
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    // 用户登录
    public User login(String username, String password) throws IOException {
        // 模拟API调用
        System.out.println("模拟登录API调用: " + username);

        // 返回模拟用户
        return User.builder()
                .id(1L)
                .username(username)
                .fullName("模拟用户")
                .email(username + "@example.com")
                .role("ROLE_USER")
                .active(true)
                .build();
    }

    // 获取交易列表
    public List<Transaction> getTransactions(Long userId, int page, int size) throws IOException {
        // 模拟API调用
        System.out.println("模拟获取交易列表: 用户ID=" + userId + ", 页码=" + page + ", 每页大小=" + size);

        // 返回模拟数据
        return MockDataService.getMockTransactions();
    }

    // 获取类别列表
    public List<Category> getCategories(Long userId) throws IOException {
        // 模拟API调用
        System.out.println("模拟获取类别列表: 用户ID=" + userId);

        // 返回模拟数据
        return MockDataService.getMockCategories();
    }

    /**
     * 批量创建交易记录
     * @param transactions 交易记录列表
     * @return 创建后的交易记录列表
     * @throws IOException IO异常
     */
    public List<Transaction> createTransactions(List<Transaction> transactions) throws IOException {
        // 模拟批量API调用
        System.out.println("模拟批量创建 " + transactions.size() + " 条交易记录");

        List<Transaction> createdTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            // 设置模拟ID
            transaction.setId(System.currentTimeMillis() + createdTransactions.size());

            // 添加到模拟数据
            MockDataService.addTransaction(transaction);

            createdTransactions.add(transaction);
        }

        return createdTransactions;
    }
    // 创建交易
    public Transaction createTransaction(Transaction transaction) throws IOException {
        // 模拟API调用
        System.out.println("模拟创建交易: " + transaction.getDescription());

        // 设置模拟ID和时间戳
        transaction.setId(System.currentTimeMillis());
        // 重要：将新交易添加到模拟数据列表中
        MockDataService.addTransaction(transaction);
        return transaction;
    }

    // 更新交易
    public Transaction updateTransaction(Transaction transaction) throws IOException {
        // 模拟API调用
        System.out.println("模拟更新交易: ID=" + transaction.getId());

        return transaction;
    }

    // 删除交易
    public boolean deleteTransaction(Long transactionId) throws IOException {
        // 模拟API调用
        System.out.println("模拟删除交易: ID=" + transactionId);

        // 实际调用MockDataService删除交易
        return MockDataService.deleteTransaction(transactionId);
    }
}