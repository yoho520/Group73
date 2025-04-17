package com.example.software.financeapp.service;

import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.CategoryType;
import com.example.software.financeapp.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模拟数据服务类 - 提供模拟数据用于开发和测试
 */
public class MockDataService {
    // 存储模拟交易数据的静态集合
    private static final List<Transaction> mockTransactions = new ArrayList<>();
    private static final List<Category> mockCategories = new ArrayList<>();
    private static boolean isInitialized = false;

    // 模拟用户
    private static final User mockUser = User.builder()
            .id(1L)
            .username("demo")
            .fullName("演示用户")
            .email("demo@example.com")
            .role("ROLE_USER")
            .active(true)
            .build();

    /**
     * 初始化模拟数据 - 只在第一次调用时执行
     */
    private static synchronized void initializeMockData() {
        if (isInitialized) {
            return;
        }

        // 先创建所有类别
        createMockCategories();

        // 再创建交易记录，此时类别已经存在
        createMockTransactions();

        isInitialized = true;
    }

    /**
     * 创建模拟类别
     */
    private static void createMockCategories() {
        // 支出类别
        mockCategories.add(Category.builder()
                .id(1L)
                .name("餐饮")
                .description("吃饭、外卖等")
                .type(CategoryType.EXPENSE)
                .icon("food_icon")
                .color("#e74c3c")
                .systemDefault(true)
                .keywords("餐厅,饭店,食堂,外卖,美食,餐饮,吃饭")
                .build());

        mockCategories.add(Category.builder()
                .id(2L)
                .name("交通")
                .description("公共交通、打车等")
                .type(CategoryType.EXPENSE)
                .icon("transport_icon")
                .color("#3498db")
                .systemDefault(true)
                .keywords("地铁,公交,出租车,打车,高铁,火车,机票")
                .build());

        mockCategories.add(Category.builder()
                .id(3L)
                .name("购物")
                .description("日用品、服装等")
                .type(CategoryType.EXPENSE)
                .icon("shopping_icon")
                .color("#9b59b6")
                .systemDefault(true)
                .keywords("超市,商场,淘宝,京东,购物,服装,日用品")
                .build());

        mockCategories.add(Category.builder()
                .id(4L)
                .name("娱乐")
                .description("电影、游戏等")
                .type(CategoryType.EXPENSE)
                .icon("entertainment_icon")
                .color("#f39c12")
                .systemDefault(true)
                .keywords("电影,游戏,娱乐,ktv,演唱会")
                .build());

        // 收入类别
        mockCategories.add(Category.builder()
                .id(5L)
                .name("工资")
                .description("固定工资收入")
                .type(CategoryType.INCOME)
                .icon("salary_icon")
                .color("#2ecc71")
                .systemDefault(true)
                .keywords("工资,薪资,薪水,月薪,工资条")
                .build());

        mockCategories.add(Category.builder()
                .id(6L)
                .name("奖金")
                .description("奖金、绩效等")
                .type(CategoryType.INCOME)
                .icon("bonus_icon")
                .color("#1abc9c")
                .systemDefault(true)
                .keywords("奖金,绩效,年终奖,提成,补贴")
                .build());
    }

    /**
     * 直接从列表中通过ID查找类别，不调用getMockCategories()
     */
    private static Category getCategory(Long id) {
        for (Category category : mockCategories) {
            if (category.getId().equals(id)) {
                return category;
            }
        }
        return null;
    }

    /**
     * 创建模拟交易
     */
    private static void createMockTransactions() {
        // 支出交易
        mockTransactions.add(Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("35.50"))
                .description("午餐 - 盖浇饭")
                .merchant("学校食堂")
                .transactionDate(LocalDateTime.now().minusDays(1))
                .category(getCategory(1L))  // 餐饮
                .source("手动输入")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(2L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("15.00"))
                .description("地铁")
                .merchant("上海地铁")
                .transactionDate(LocalDateTime.now().minusDays(1))
                .category(getCategory(2L))  // 交通
                .source("手动输入")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(3L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("199.99"))
                .description("新衣服")
                .merchant("优衣库")
                .transactionDate(LocalDateTime.now().minusDays(2))
                .category(getCategory(3L))  // 购物
                .source("支付宝")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(4L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("70.00"))
                .description("电影票两张")
                .merchant("万达影城")
                .transactionDate(LocalDateTime.now().minusDays(3))
                .category(getCategory(4L))  // 娱乐
                .source("微信支付")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());

        // 收入交易
        mockTransactions.add(Transaction.builder()
                .id(5L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("6000.00"))
                .description("4月工资")
                .merchant("公司")
                .transactionDate(LocalDateTime.now().minusDays(5))
                .category(getCategory(5L))  // 工资
                .source("手动输入")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(6L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("1000.00"))
                .description("季度绩效奖金")
                .merchant("公司")
                .transactionDate(LocalDateTime.now().minusDays(6))
                .category(getCategory(6L))  // 奖金
                .source("手动输入")
                .user(mockUser)
                .categoryConfirmed(true)
                .build());
    }

    /**
     * 获取模拟类别列表
     */
    public static List<Category> getMockCategories() {
        if (!isInitialized) {
            initializeMockData();
        }
        return new ArrayList<>(mockCategories);
    }

    /**
     * 获取模拟交易列表
     */
    public static List<Transaction> getMockTransactions() {
        if (!isInitialized) {
            initializeMockData();
        }

        // 返回交易列表的副本，按日期降序排序
        List<Transaction> sortedTransactions = new ArrayList<>(mockTransactions);
        Collections.sort(sortedTransactions, Comparator.comparing(Transaction::getTransactionDate).reversed());
        return sortedTransactions;
    }

    /**
     * 获取指定用户的模拟交易列表
     */
    public static List<Transaction> getMockTransactionsForUser(Long userId) {
        return getMockTransactions().stream()
                .filter(t -> t.getUser() != null && t.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查找交易
     */
    public static Transaction findTransactionById(Long id) {
        if (!isInitialized) {
            initializeMockData();
        }

        return mockTransactions.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据ID查找类别
     */
    public static Category findCategoryById(Long id) {
        if (!isInitialized) {
            initializeMockData();
        }

        return mockCategories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加新交易
     */
    public static Transaction addTransaction(Transaction transaction) {
        if (!isInitialized) {
            initializeMockData();
        }

        // 确保有ID
        if (transaction.getId() == null) {
            // 生成简单的模拟ID（实际项目中应使用更可靠的ID生成策略）
            long maxId = mockTransactions.stream()
                    .mapToLong(t -> t.getId() != null ? t.getId() : 0)
                    .max()
                    .orElse(0);
            transaction.setId(maxId + 1);
        }

        // 添加到集合
        mockTransactions.add(transaction);

        System.out.println("Added new transaction: ID=" + transaction.getId() +
                ", Type=" + transaction.getType() +
                ", Amount=" + transaction.getAmount());

        return transaction;
    }

    /**
     * 更新现有交易
     */
    public static Transaction updateTransaction(Transaction transaction) {
        if (!isInitialized) {
            initializeMockData();
        }

        if (transaction.getId() == null) {
            throw new IllegalArgumentException("Cannot update transaction without ID");
        }

        // 查找并删除旧交易
        mockTransactions.removeIf(t -> t.getId().equals(transaction.getId()));

        // 添加更新后的交易
        mockTransactions.add(transaction);

        System.out.println("Updated transaction: ID=" + transaction.getId());

        return transaction;
    }

    /**
     * 删除交易
     */
    public static boolean deleteTransaction(Long id) {
        if (!isInitialized) {
            initializeMockData();
        }

        int sizeBefore = mockTransactions.size();
        mockTransactions.removeIf(t -> t.getId().equals(id));

        boolean deleted = mockTransactions.size() < sizeBefore;
        if (deleted) {
            System.out.println("Deleted transaction: ID=" + id);
        }

        return deleted;
    }

    /**
     * 获取当前月份的收入总额
     */
    public static BigDecimal getTotalIncomeForCurrentMonth() {
        if (!isInitialized) {
            initializeMockData();
        }

        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        return mockTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .filter(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取当前月份的支出总额
     */
    public static BigDecimal getTotalExpenseForCurrentMonth() {
        if (!isInitialized) {
            initializeMockData();
        }

        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        return mockTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取模拟用户
     */
    public static User getMockUser() {
        return mockUser;
    }
}