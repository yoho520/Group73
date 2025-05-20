package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.MockDataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FamilyFinanceController implements Initializable {

    @FXML
    private ComboBox<User> familyMemberComboBox;

    @FXML
    private Label currentMemberLabel;

    @FXML
    private Label incomeLabel;

    @FXML
    private Label expenseLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Label transactionCountLabel;

    @FXML
    private PieChart expenseCategoryChart;

    @FXML
    private BarChart<String, Number> incomeExpenseChart;

    @FXML
    private VBox recentTransactionsContainer;

    @FXML
    private VBox parentAdviceContainer;

    @FXML
    private VBox adviceItemsContainer;

    @FXML
    private Button viewAllTransactionsButton;

    private ApiService apiService;
    private User currentUser;
    private User selectedMember;
    private List<Transaction> currentTransactions = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = AppContext.getInstance().getApiService();
        currentUser = AppContext.getInstance().getCurrentUser();

        // 初始化家庭成员下拉框
        initializeFamilyMembersComboBox();

        // 默认显示当前用户的财务情况
        selectedMember = currentUser;
        currentMemberLabel.setText(selectedMember.getFullName());

        // 加载数据
        loadFinancialData();

        // 设置父母建议区域的可见性
        boolean isParent = isParentUser();
        boolean viewingChild = isViewingChildAccount();
        parentAdviceContainer.setVisible(isParent && viewingChild);
        parentAdviceContainer.setManaged(isParent && viewingChild);
    }

    /**
     * 初始化家庭成员下拉框
     */
    private void initializeFamilyMembersComboBox() {
        List<User> familyMembers = new ArrayList<>();

        // 添加当前用户
        familyMembers.add(currentUser);

        // 如果是父亲账户，添加所有子女
        if (isParentUser()) {
            familyMembers.addAll(MockDataService.getFamilyMembersUnderGuardianship(currentUser.getId()));
        }

        // 设置下拉框数据
        familyMemberComboBox.setItems(FXCollections.observableArrayList(familyMembers));

        // 设置单元格工厂，显示用户全名
        familyMemberComboBox.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                    // 如果是当前用户，在名字后添加"(我)"
                    if (item.getId().equals(currentUser.getId())) {
                        setText(item.getFullName() + " (我)");
                    }
                }
            }
        });

        // 设置显示的文本
        familyMemberComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName());
                    if (item.getId().equals(currentUser.getId())) {
                        setText(item.getFullName() + " (我)");
                    }
                }
            }
        });

        // 默认选中当前用户
        familyMemberComboBox.getSelectionModel().select(currentUser);
    }

    /**
     * 处理家庭成员选择
     */
    @FXML
    private void handleMemberSelection() {
        User selected = familyMemberComboBox.getSelectionModel().getSelectedItem();

        if (selected != null) {
            selectedMember = selected;
            currentMemberLabel.setText(selectedMember.getFullName());

            // 重新加载数据
            loadFinancialData();

            // 更新父母建议区域的可见性
            boolean isParent = isParentUser();
            boolean viewingChild = isViewingChildAccount();
            parentAdviceContainer.setVisible(isParent && viewingChild);
            parentAdviceContainer.setManaged(isParent && viewingChild);
        }
    }

    /**
     * 加载财务数据
     */
    private void loadFinancialData() {
        try {
            // 获取所选用户的交易数据
            if (MockDataService.hasPermissionToView(currentUser.getId(), selectedMember.getId())) {
                // 获取特定用户的交易数据
                currentTransactions = MockDataService.getMockTransactionsForUser(selectedMember.getId());

                // 更新财务概况
                updateFinancialOverview();

                // 更新图表
                updateCharts();

                // 更新最近交易列表
                updateRecentTransactions();

                // 更新建议内容（如果是父亲查看儿子账户）
                if (isParentUser() && isViewingChildAccount()) {
                    updateParentAdvice();
                } else {
                    // 如果不是父亲查看儿子账户，隐藏建议区域
                    parentAdviceContainer.setVisible(false);
                    parentAdviceContainer.setManaged(false);
                }
            } else {
                showAlert("没有权限", "您没有权限查看此用户的财务信息");
                // 重置为当前用户
                selectedMember = currentUser;
                familyMemberComboBox.getSelectionModel().select(currentUser);
                currentMemberLabel.setText(currentUser.getFullName());
                loadFinancialData(); // 重新加载当前用户数据
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("加载失败", "无法加载财务数据: " + e.getMessage());
        }
    }

    /**
     * 更新财务概况
     */
    private void updateFinancialOverview() {
        // 获取当前月份
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 过滤当月交易
        List<Transaction> currentMonthTransactions = currentTransactions.stream()
                .filter(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .collect(Collectors.toList());

        // 计算收入总额
        BigDecimal totalIncome = currentMonthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算支出总额
        BigDecimal totalExpense = currentMonthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算结余
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 更新UI
        incomeLabel.setText(String.format("¥%.2f", totalIncome));
        expenseLabel.setText(String.format("¥%.2f", totalExpense));
        balanceLabel.setText(String.format("¥%.2f", balance));

        // 根据结余情况设置颜色
        if (balance.compareTo(BigDecimal.ZERO) >= 0) {
            balanceLabel.setTextFill(Color.valueOf("#2ecc71")); // 绿色
        } else {
            balanceLabel.setTextFill(Color.valueOf("#e74c3c")); // 红色
        }

        // 设置交易笔数
        transactionCountLabel.setText(String.valueOf(currentMonthTransactions.size()));
    }

    /**
     * 更新图表
     */
    private void updateCharts() {
        // 清空现有图表数据
        expenseCategoryChart.getData().clear();
        incomeExpenseChart.getData().clear();

        // 更新支出类别饼图
        updateExpenseCategoryChart();

        // 更新收支柱状图
        updateIncomeExpenseChart();
    }

    /**
     * 更新支出类别饼图
     */
    private void updateExpenseCategoryChart() {
        // 按类别分组统计支出
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();

        for (Transaction t : currentTransactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                String categoryName = t.getCategory().getName();
                BigDecimal currentAmount = expenseByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                expenseByCategory.put(categoryName, currentAmount.add(t.getAmount()));
            }
        }

        // 创建饼图数据
        for (Map.Entry<String, BigDecimal> entry : expenseByCategory.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + ": ¥" + entry.getValue().toString(),
                    entry.getValue().doubleValue());
            expenseCategoryChart.getData().add(slice);
        }
    }

    /**
     * 更新收支柱状图
     */
    private void updateIncomeExpenseChart() {
        // 获取最近7天的日期
        List<LocalDate> last7Days = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            last7Days.add(today.minusDays(i));
        }

        // 创建收入系列
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("收入");

        // 创建支出系列
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("支出");

        // 日期格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // 为每一天计算收入和支出
        for (LocalDate date : last7Days) {
            final LocalDate currentDate = date;

            // 当天收入
            BigDecimal dayIncome = currentTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .filter(t -> t.getTransactionDate().toLocalDate().equals(currentDate))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 当天支出
            BigDecimal dayExpense = currentTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .filter(t -> t.getTransactionDate().toLocalDate().equals(currentDate))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 添加到系列
            String dateStr = date.format(formatter);
            incomeSeries.getData().add(new XYChart.Data<>(dateStr, dayIncome));
            expenseSeries.getData().add(new XYChart.Data<>(dateStr, dayExpense));
        }

        // 添加系列到图表
        incomeExpenseChart.getData().addAll(incomeSeries, expenseSeries);

        // 设置样式
        for (XYChart.Series<String, Number> series : incomeExpenseChart.getData()) {
            if ("收入".equals(series.getName())) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: #2ecc71;"); // 绿色
                }
            } else {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    data.getNode().setStyle("-fx-bar-fill: #e74c3c;"); // 红色
                }
            }
        }
    }

    /**
     * 更新最近交易列表
     */
    private void updateRecentTransactions() {
        recentTransactionsContainer.getChildren().clear();

        // 获取最近5笔交易
        List<Transaction> recentTransactions = currentTransactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (recentTransactions.isEmpty()) {
            Label emptyLabel = new Label("暂无交易记录");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            recentTransactionsContainer.getChildren().add(emptyLabel);
            return;
        }

        // 为每笔交易创建卡片
        for (Transaction transaction : recentTransactions) {
            HBox transactionRow = createTransactionRow(transaction);
            recentTransactionsContainer.getChildren().add(transactionRow);
        }
    }

    /**
     * 创建交易行
     */
    private HBox createTransactionRow(Transaction transaction) {
        HBox row = new HBox();
        row.setSpacing(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 5, 8, 5));
        row.setStyle("-fx-border-color: #f1f1f1; -fx-border-width: 0 0 1 0;");

        // 日期和时间
        VBox dateTimeBox = new VBox(2);
        dateTimeBox.setPrefWidth(120);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Label dateLabel = new Label(transaction.getTransactionDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-weight: bold;");

        Label timeLabel = new Label(transaction.getTransactionDate().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");

        dateTimeBox.getChildren().addAll(dateLabel, timeLabel);

        // 交易描述和商家
        VBox descriptionBox = new VBox(2);
        HBox.setHgrow(descriptionBox, Priority.ALWAYS);

        Label descriptionLabel = new Label(transaction.getDescription());
        descriptionLabel.setStyle("-fx-font-weight: bold;");

        Label merchantLabel = new Label(transaction.getMerchant() != null ? transaction.getMerchant() : "");
        merchantLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");

        descriptionBox.getChildren().addAll(descriptionLabel, merchantLabel);

        // 类别
        Label categoryLabel = new Label();
        if (transaction.getCategory() != null) {
            categoryLabel.setText(transaction.getCategory().getName());
            categoryLabel.setStyle(
                    "-fx-background-color: " + transaction.getCategory().getColor() + "22; " +
                            "-fx-padding: 3 8; " +
                            "-fx-background-radius: 3; " +
                            "-fx-text-fill: " + transaction.getCategory().getColor() + ";"
            );
        }

        // 金额
        Label amountLabel = new Label(String.format("¥%.2f", transaction.getAmount()));
        amountLabel.setPrefWidth(100);
        amountLabel.setStyle("-fx-font-weight: bold; -fx-alignment: CENTER_RIGHT;");

        // 根据交易类型设置金额颜色
        if (transaction.getType() == TransactionType.INCOME) {
            amountLabel.setTextFill(Color.valueOf("#2ecc71")); // 绿色
            amountLabel.setText("+" + amountLabel.getText());
        } else {
            amountLabel.setTextFill(Color.valueOf("#e74c3c")); // 红色
            amountLabel.setText("-" + amountLabel.getText());
        }

        // 组装行
        row.getChildren().addAll(dateTimeBox, descriptionBox, categoryLabel, amountLabel);

        return row;
    }

    /**
     * 更新父母建议内容
     */
    private void updateParentAdvice() {
        adviceItemsContainer.getChildren().clear();

        // 计算儿子账户的财务指标
        BigDecimal monthlyIncome = BigDecimal.ZERO;
        BigDecimal monthlyExpense = BigDecimal.ZERO;

        // 当前月份
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // 过滤当月交易
        List<Transaction> currentMonthTransactions = currentTransactions.stream()
                .filter(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return date.getMonthValue() == currentMonth && date.getYear() == currentYear;
                })
                .collect(Collectors.toList());

        // 计算收入和支出
        for (Transaction t : currentMonthTransactions) {
            if (t.getType() == TransactionType.INCOME) {
                monthlyIncome = monthlyIncome.add(t.getAmount());
            } else {
                monthlyExpense = monthlyExpense.add(t.getAmount());
            }
        }

        // 生成建议
        List<Map<String, String>> adviceList = new ArrayList<>();

        // 1. 收支平衡建议
        if (monthlyExpense.compareTo(monthlyIncome) > 0) {
            Map<String, String> advice = new HashMap<>();
            advice.put("title", "月度支出超过收入");
            advice.put("content", "本月支出（¥" + monthlyExpense + "）已超过收入（¥" + monthlyIncome + "），建议降低非必要支出。");
            advice.put("type", "warning");
            adviceList.add(advice);
        }

        // 2. 大额支出提醒
        BigDecimal largeExpenseThreshold = new BigDecimal("200"); // 设定大额支出阈值
        List<Transaction> largeExpenses = currentMonthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && t.getAmount().compareTo(largeExpenseThreshold) > 0)
                .collect(Collectors.toList());

        if (!largeExpenses.isEmpty()) {
            Map<String, String> advice = new HashMap<>();
            advice.put("title", "大额支出提醒");
            advice.put("content", "本月有 " + largeExpenses.size() + " 笔大额支出（超过¥" + largeExpenseThreshold + "），请关注孩子的消费习惯。");
            advice.put("type", "info");
            adviceList.add(advice);
        }

        // 3. 储蓄建议
        if (monthlyIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRatio = BigDecimal.ONE.subtract(monthlyExpense.divide(monthlyIncome, 2, BigDecimal.ROUND_HALF_UP));
            if (savingsRatio.compareTo(new BigDecimal("0.1")) < 0) { // 储蓄率低于10%
                Map<String, String> advice = new HashMap<>();
                advice.put("title", "培养储蓄习惯");
                advice.put("content", "当前储蓄率较低（" + savingsRatio.multiply(new BigDecimal("100")).intValue() + "%），建议引导孩子培养良好的储蓄习惯。");
                advice.put("type", "suggestion");
                adviceList.add(advice);
            }
        }

        // 4. 消费类别分析
        Map<String, BigDecimal> categoryExpenses = new HashMap<>();
        for (Transaction t : currentMonthTransactions) {
            if (t.getType() == TransactionType.EXPENSE && t.getCategory() != null) {
                String categoryName = t.getCategory().getName();
                BigDecimal amount = categoryExpenses.getOrDefault(categoryName, BigDecimal.ZERO);
                categoryExpenses.put(categoryName, amount.add(t.getAmount()));
            }
        }

        // 找出最高支出类别
        String highestCategory = "";
        BigDecimal highestAmount = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : categoryExpenses.entrySet()) {
            if (entry.getValue().compareTo(highestAmount) > 0) {
                highestCategory = entry.getKey();
                highestAmount = entry.getValue();
            }
        }

        if (!highestCategory.isEmpty() && monthlyExpense.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = highestAmount.divide(monthlyExpense, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));

            Map<String, String> advice = new HashMap<>();
            advice.put("title", "支出集中在 " + highestCategory + " 类别");
            advice.put("content", "约 " + percentage.intValue() + "% 的支出用于 " + highestCategory + "（¥" + highestAmount + "），可引导孩子更均衡地分配资金。");
            advice.put("type", "info");
            adviceList.add(advice);
        }

        // 如果没有建议，添加一个默认消息
        if (adviceList.isEmpty()) {
            Map<String, String> advice = new HashMap<>();
            advice.put("title", "财务状况良好");
            advice.put("content", "孩子当前的财务状况良好，继续保持良好的理财习惯。");
            advice.put("type", "success");
            adviceList.add(advice);
        }

        // 创建建议卡片
        for (Map<String, String> advice : adviceList) {
            VBox adviceCard = createAdviceCard(advice);
            adviceItemsContainer.getChildren().add(adviceCard);
        }
    }

    /**
     * 创建建议卡片
     */
    private VBox createAdviceCard(Map<String, String> advice) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10, 15, 10, 15));
        card.setStyle("-fx-background-radius: 5; -fx-border-radius: 5;");

        // 根据类型设置样式
        String type = advice.getOrDefault("type", "info");
        switch (type) {
            case "warning":
                card.setStyle(card.getStyle() + "-fx-background-color: #fff3e0; -fx-border-color: #ffa726;");
                break;
            case "success":
                card.setStyle(card.getStyle() + "-fx-background-color: #e8f5e9; -fx-border-color: #66bb6a;");
                break;
            case "suggestion":
                card.setStyle(card.getStyle() + "-fx-background-color: #e1f5fe; -fx-border-color: #29b6f6;");
                break;
            default: // info
                card.setStyle(card.getStyle() + "-fx-background-color: #f5f5f5; -fx-border-color: #bdbdbd;");
        }

        Label titleLabel = new Label(advice.get("title"));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label contentLabel = new Label(advice.get("content"));
        contentLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, contentLabel);

        return card;
    }

    /**
     * 处理查看全部交易
     */
    @FXML
    private void handleViewAllTransactions() {
        try {
            // 如果正在查看其他家庭成员的财务，先切换到该成员
            if (selectedMember != null && !selectedMember.getId().equals(currentUser.getId())) {
                // 这里应该将选定的家庭成员ID保存到某个地方，以便交易页面知道显示谁的交易
                // 例如可以通过AppContext或其他方式
                AppContext context = AppContext.getInstance();
                context.setCurrentUser(selectedMember); // 临时更改为要查看的用户

                // 导航到交易页面
                navigateToTransactions();

                // 还原当前用户
                context.setCurrentUser(currentUser);
            } else {
                // 直接导航到交易页面
                navigateToTransactions();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("导航错误", "无法打开交易页面: " + e.getMessage());
        }
    }

    /**
     * 导航到交易页面
     */
    private void navigateToTransactions() throws IOException {
        // 清除当前内容区域内容
        BorderPane borderPane = (BorderPane) recentTransactionsContainer.getScene().getRoot();
        StackPane contentArea = (StackPane) borderPane.getParent().getParent();
        contentArea.getChildren().clear();

        // 加载交易视图
        Parent transactionsView = FXMLLoader.load(getClass().getResource("/view/transactions.fxml"));
        contentArea.getChildren().add(transactionsView);
    }

    /**
     * 处理刷新按钮
     */
    @FXML
    private void handleRefresh() {
        loadFinancialData();
    }

    /**
     * 检查当前用户是否为父亲（有监护权限）
     */
    private boolean isParentUser() {
        return currentUser.getId().equals(1L); // 父亲ID为1
    }

    /**
     * 检查是否正在查看儿子账户
     */
    private boolean isViewingChildAccount() {
        return selectedMember != null && selectedMember.getId().equals(2L); // 儿子ID为2
    }

    /**
     * 显示错误提示对话框
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}