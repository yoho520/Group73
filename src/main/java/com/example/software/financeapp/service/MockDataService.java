package com.example.software.financeapp.service;

import com.example.software.financeapp.model.entity.*;
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
    // 添加到现有的MockDataService类中的静态数据区域
    private static List<SavingsTier> mockSavingsTiers = new ArrayList<>();
    private static List<SavingsGoal> mockSavingsGoals = new ArrayList<>();
    // 家庭关系集合
    private static final List<FamilyRelationship> mockFamilyRelationships = new ArrayList<>();

// 添加到MockDataService类中
    /**
     * 获取模拟储蓄层级数据
     */
    public static List<SavingsTier> getMockSavingsTiers(Long userId) {
        // 过滤出指定用户的层级
        return mockSavingsTiers.stream()
                .filter(tier -> tier.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 添加模拟储蓄层级
     */
    public static void addSavingsTier(SavingsTier tier) {
        mockSavingsTiers.add(tier);
    }

    /**
     * 更新模拟储蓄层级
     */
    public static void updateSavingsTier(SavingsTier tier) {
        for (int i = 0; i < mockSavingsTiers.size(); i++) {
            if (mockSavingsTiers.get(i).getId().equals(tier.getId())) {
                mockSavingsTiers.set(i, tier);
                break;
            }
        }
    }

    /**
     * 获取模拟储蓄目标数据
     */
    public static List<SavingsGoal> getMockSavingsGoals(Long userId) {
        // 过滤出指定用户的目标
        return mockSavingsGoals.stream()
                .filter(goal -> goal.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取特定储蓄目标
     */
    public static SavingsGoal getMockSavingsGoal(Long goalId) {
        return mockSavingsGoals.stream()
                .filter(goal -> goal.getId().equals(goalId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加模拟储蓄目标
     */
    public static void addSavingsGoal(SavingsGoal goal) {
        mockSavingsGoals.add(goal);
    }

    /**
     * 更新模拟储蓄目标
     */
    public static void updateSavingsGoal(SavingsGoal goal) {
        for (int i = 0; i < mockSavingsGoals.size(); i++) {
            if (mockSavingsGoals.get(i).getId().equals(goal.getId())) {
                mockSavingsGoals.set(i, goal);
                break;
            }
        }
    }
    // 模拟用户
    // 模拟用户列表
    private static final List<User> mockUsers = new ArrayList<>();

    // 修改为变量而非常量
    private static User mockUser;

    // 静态初始化块
    static {
        // 爸爸用户
        User fatherUser = User.builder()
                .id(1L)
                .username("father")
                .password("password")  // 在实际应用中应该加密存储
                .fullName("张爸爸")
                .email("father@example.com")
                .role("ROLE_USER")
                .active(true)
                .build();

        // 儿子用户
        User sonUser = User.builder()
                .id(2L)
                .username("son")
                .password("password")  // 在实际应用中应该加密存储
                .fullName("张儿子")
                .email("son@example.com")
                .role("ROLE_USER")
                .active(true)
                .build();

        // 添加到用户列表
        mockUsers.add(fatherUser);
        mockUsers.add(sonUser);

        // 设置当前模拟用户为爸爸
        mockUser = fatherUser;


        // 创建父子关系（父亲ID=1，儿子ID=2）
        FamilyRelationship fatherSonRelationship = FamilyRelationship.builder()
                .id(1L)
                .parentId(1L)  // 父亲ID
                .childId(2L)   // 儿子ID
                .relationshipType("父子")
                .permissionLevel("完全访问")
                .createdAt(LocalDateTime.now())
                .status("活跃")
                .build();

        mockFamilyRelationships.add(fatherSonRelationship);

    }

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
        // 获取父亲和儿子用户对象
        User fatherUser = mockUsers.get(0); // 父亲
        User sonUser = mockUsers.get(1);    // 儿子

        // 父亲的交易数据
        mockTransactions.add(Transaction.builder()
                .id(1L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("35.50"))
                .description("午餐 - 盖浇饭")
                .merchant("学校食堂")
                .transactionDate(LocalDateTime.now().minusDays(1))
                .category(getCategory(1L))  // 餐饮
                .source("手动输入")
                .user(fatherUser)
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
                .user(fatherUser)
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
                .user(fatherUser)
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
                .user(fatherUser)
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
                .user(fatherUser)
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
                .user(fatherUser)
                .categoryConfirmed(true)
                .build());

        // 儿子的交易数据
        mockTransactions.add(Transaction.builder()
                .id(7L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("25.50"))
                .description("午餐 - 牛肉面")
                .merchant("校园食堂")
                .transactionDate(LocalDateTime.now().minusDays(1))
                .category(getCategory(1L))  // 餐饮
                .source("手动输入")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(8L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("12.00"))
                .description("公交车")
                .merchant("公交公司")
                .transactionDate(LocalDateTime.now().minusDays(2))
                .category(getCategory(2L))  // 交通
                .source("手动输入")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(9L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("89.99"))
                .description("篮球")
                .merchant("体育用品店")
                .transactionDate(LocalDateTime.now().minusDays(3))
                .category(getCategory(4L))  // 娱乐
                .source("支付宝")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(10L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("45.50"))
                .description("教辅材料")
                .merchant("新华书店")
                .transactionDate(LocalDateTime.now().minusDays(4))
                .category(getCategory(3L))  // 购物
                .source("微信支付")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(11L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500.00"))
                .description("本月零花钱")
                .merchant("家庭")
                .transactionDate(LocalDateTime.now().minusDays(5))
                .category(getCategory(5L))  // 工资（作为收入类别）
                .source("手动输入")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(12L)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("20.00"))
                .description("剪头发")
                .merchant("理发店")
                .transactionDate(LocalDateTime.now().minusDays(7))
                .category(getCategory(3L))  // 购物（个人护理）
                .source("现金")
                .user(sonUser)
                .categoryConfirmed(true)
                .build());

        mockTransactions.add(Transaction.builder()
                .id(13L)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100.00"))
                .description("期中考试奖励")
                .merchant("家庭")
                .transactionDate(LocalDateTime.now().minusDays(10))
                .category(getCategory(6L))  // 奖金
                .source("手动输入")
                .user(sonUser)
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

    /**
     * 根据用户名查找用户
     */
    public static User findUserByUsername(String username) {
        return mockUsers.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * 验证用户凭证
     */
    public static User authenticateUser(String username, String password) {
        return mockUsers.stream()
                .filter(user -> user.getUsername().equals(username) &&
                        user.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有模拟用户
     */
    public static List<User> getMockUsers() {
        return new ArrayList<>(mockUsers);
    }


    /**
     * 获取用户的家庭关系
     */
    public static List<FamilyRelationship> getFamilyRelationshipsForUser(Long userId) {
        return mockFamilyRelationships.stream()
                .filter(r -> r.getParentId().equals(userId) || r.getChildId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户监护的家庭成员
     */
    public static List<User> getFamilyMembersUnderGuardianship(Long guardianId) {
        List<Long> childIds = mockFamilyRelationships.stream()
                .filter(r -> r.getParentId().equals(guardianId) && "活跃".equals(r.getStatus()))
                .map(FamilyRelationship::getChildId)
                .collect(Collectors.toList());

        return mockUsers.stream()
                .filter(user -> childIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否有权限查看另一用户的财务
     */
    public static boolean hasPermissionToView(Long viewerId, Long targetId) {
        // 相同用户ID，自己查看自己的财务
        if (viewerId.equals(targetId)) {
            return true;
        }

        // 检查家庭关系
        return mockFamilyRelationships.stream()
                .anyMatch(r -> r.getParentId().equals(viewerId) &&
                        r.getChildId().equals(targetId) &&
                        "活跃".equals(r.getStatus()));
    }
}