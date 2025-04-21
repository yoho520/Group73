package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.analysis.ExpenditureAnalysisService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

/**
 * 支出洞察与预算建议控制器
 */
public class BudgetInsightsController implements Initializable {

    @FXML
    private ComboBox<String> timeRangeComboBox;

    @FXML
    private LineChart<String, Number> spendingTrendChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private PieChart categoryDistributionChart;

    @FXML
    private Label avgMonthlySpendingLabel;

    @FXML
    private Label spendingTrendLabel;

    @FXML
    private VBox topGrowthCategoriesContainer;

    @FXML
    private VBox budgetRecommendationsContainer;

    @FXML
    private VBox savingSuggestionsContainer;

    @FXML
    private Button applyRecommendationsButton;

    // 服务和应用程序上下文
    private final AppContext appContext = AppContext.getInstance();
    private ApiService apiService;
    private ExpenditureAnalysisService analysisService;

    // 数据相关字段
    private List<Transaction> transactions;
    private Map<YearMonth, BigDecimal> monthlyTrendData;
    private ExpenditureAnalysisService.BudgetRecommendation currentRecommendation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.analysisService = new ExpenditureAnalysisService();

        // 初始化时间范围选择器
        timeRangeComboBox.setItems(FXCollections.observableArrayList(
                "最近3个月", "最近6个月", "最近12个月", "今年", "去年"
        ));
        timeRangeComboBox.setValue("最近6个月");
        timeRangeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());

        // 初始化图表属性
        spendingTrendChart.setTitle("月度支出趋势");
        categoryDistributionChart.setTitle("类别支出分布");

        // 设置应用建议按钮点击事件
        applyRecommendationsButton.setOnAction(event -> applyRecommendations());

        // 加载初始数据
        loadData();
    }

    /**
     * 加载数据
     */
    /**
     * 加载数据
     */
    private void loadData() {
        try {
            // 获取当前用户
            User currentUser = appContext.getCurrentUser();
            if (currentUser == null) return;

            // 首先获取所有交易记录（使用分页参数）
            // 这里使用0作为页码，1000作为大小以获取足够多的记录
            List<Transaction> allTransactions = apiService.getTransactions(currentUser.getId(), 0, 1000);

            // 然后根据选择的时间范围在内存中过滤
            int monthsCount = getSelectedMonthsCount();
            LocalDate startDate = LocalDate.now().minusMonths(monthsCount).withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();

            // 过滤交易记录
            transactions = allTransactions.stream()
                    .filter(t -> {
                        LocalDate transactionDate = t.getTransactionDate().toLocalDate();
                        return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
                    })
                    .collect(Collectors.toList());

            // 刷新分析和可视化
            refreshData();

        } catch (IOException e) {
            showErrorAlert("加载数据失败", e.getMessage());
        }
    }

    /**
     * 刷新数据分析和可视化
     */
    /**
     * 刷新数据分析和可视化
     */
    private void refreshData() {
        if (transactions == null || transactions.isEmpty()) {
            showNoDataMessage();
            return;
        }

        int monthsCount = getSelectedMonthsCount();

        // 清除所有图表和容器内容
        spendingTrendChart.getData().clear();
        categoryDistributionChart.getData().clear();
        topGrowthCategoriesContainer.getChildren().clear();
        budgetRecommendationsContainer.getChildren().clear();
        savingSuggestionsContainer.getChildren().clear();

        // 计算月度支出趋势
        monthlyTrendData = analysisService.calculateMonthlyTrend(transactions, monthsCount);
        updateTrendChart();

        // 计算类别分布
        LocalDate startDate = LocalDate.now().minusMonths(monthsCount).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        Map<Category, BigDecimal> categoryDistribution =
                analysisService.calculateCategoryDistribution(transactions, startDate, endDate);
        updateDistributionChart(categoryDistribution);

        // 生成预算建议
        currentRecommendation = analysisService.generateBudgetRecommendation(transactions, monthsCount);
        updateInsights();

        // 强制重新布局
        spendingTrendChart.layout();
        categoryDistributionChart.layout();
    }

    /**
     * 更新趋势图表
     */
    private void updateTrendChart() {
        spendingTrendChart.getData().clear();

        // 创建数据系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("月度支出");

        // 日期格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 添加数据点
        List<YearMonth> sortedMonths = new ArrayList<>(monthlyTrendData.keySet());
        Collections.sort(sortedMonths);

        for (YearMonth month : sortedMonths) {
            BigDecimal amount = monthlyTrendData.get(month);
            series.getData().add(new XYChart.Data<>(month.format(formatter), amount));
        }

        spendingTrendChart.getData().add(series);
    }

    /**
     * 更新分布图表
     */
    private void updateDistributionChart(Map<Category, BigDecimal> categoryDistribution) {
        categoryDistributionChart.getData().clear();

        // 将数据转换为饼图数据
        List<PieChart.Data> pieChartData = new ArrayList<>();

        for (Map.Entry<Category, BigDecimal> entry : categoryDistribution.entrySet()) {
            String name = entry.getKey().getName() + " ¥" + entry.getValue();
            pieChartData.add(new PieChart.Data(name, entry.getValue().doubleValue()));
        }

        categoryDistributionChart.setData(FXCollections.observableArrayList(pieChartData));
    }

    /**
     * 更新洞察与建议区域
     */
    private void updateInsights() {
        if (currentRecommendation == null) return;

        // 更新总览数据
        avgMonthlySpendingLabel.setText(String.format("¥%.2f", currentRecommendation.getTotalMonthlyAverage()));

        // 计算趋势变化
        // 简化：对比最近两个月的变化
        List<YearMonth> months = new ArrayList<>(monthlyTrendData.keySet());
        Collections.sort(months);
        if (months.size() >= 2) {
            YearMonth currentMonth = months.get(months.size() - 1);
            YearMonth prevMonth = months.get(months.size() - 2);

            BigDecimal current = monthlyTrendData.get(currentMonth);
            BigDecimal prev = monthlyTrendData.get(prevMonth);

            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal change = current.subtract(prev).divide(prev, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
                spendingTrendLabel.setText(sign + String.format("%.1f%%", change));

                // 设置颜色 (上升为红色，下降为绿色)
                if (change.compareTo(BigDecimal.ZERO) > 0) {
                    spendingTrendLabel.setTextFill(Color.RED);
                } else {
                    spendingTrendLabel.setTextFill(Color.GREEN);
                }
            }
        }

        // 清除并更新增长最快的类别
        topGrowthCategoriesContainer.getChildren().clear();

        for (ExpenditureAnalysisService.CategoryGrowthInfo growthInfo : currentRecommendation.getTopGrowthCategories()) {
            HBox categoryRow = createCategoryGrowthRow(growthInfo);
            topGrowthCategoriesContainer.getChildren().add(categoryRow);
        }

        // 清除并更新预算建议
        budgetRecommendationsContainer.getChildren().clear();

        Map<Category, BigDecimal> recommendedBudget = currentRecommendation.generateRecommendedBudget();
        List<Map.Entry<Category, BigDecimal>> sortedBudgets = recommendedBudget.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (Map.Entry<Category, BigDecimal> entry : sortedBudgets) {
            HBox budgetRow = createBudgetRecommendationRow(entry.getKey(), entry.getValue());
            budgetRecommendationsContainer.getChildren().add(budgetRow);
        }

        // 清除并更新节省建议
        savingSuggestionsContainer.getChildren().clear();

        for (ExpenditureAnalysisService.SavingSuggestion suggestion : currentRecommendation.getSavingSuggestions()) {
            VBox suggestionBox = createSavingSuggestionBox(suggestion);
            savingSuggestionsContainer.getChildren().add(suggestionBox);
        }
    }

    /**
     * 创建类别增长行
     */
    private HBox createCategoryGrowthRow(ExpenditureAnalysisService.CategoryGrowthInfo growthInfo) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);

        Label categoryLabel = new Label(growthInfo.getCategory().getName());
        categoryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(categoryLabel, Priority.ALWAYS);

        Label growthLabel = new Label(growthInfo.getGrowthRateFormatted());
        growthLabel.getStyleClass().add("growth-rate");

        // 设置增长率颜色
        if (growthInfo.getGrowthRate().compareTo(BigDecimal.ZERO) > 0) {
            growthLabel.setTextFill(Color.RED);
        } else {
            growthLabel.setTextFill(Color.GREEN);
        }

        row.getChildren().addAll(categoryLabel, growthLabel);
        return row;
    }

    /**
     * 创建预算建议行
     */
    private HBox createBudgetRecommendationRow(Category category, BigDecimal recommendedAmount) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);

        Label categoryLabel = new Label(category.getName());
        categoryLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(categoryLabel, Priority.ALWAYS);

        Label amountLabel = new Label(String.format("¥%.2f", recommendedAmount));
        amountLabel.getStyleClass().add("recommended-amount");

        row.getChildren().addAll(categoryLabel, amountLabel);
        return row;
    }

    /**
     * 创建节省建议框
     */
    private VBox createSavingSuggestionBox(ExpenditureAnalysisService.SavingSuggestion suggestion) {
        VBox box = new VBox();
        box.setSpacing(5);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("saving-suggestion-box");

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        titleRow.setSpacing(10);

        Label categoryLabel = new Label(suggestion.getCategory().getName());
        categoryLabel.setStyle("-fx-font-weight: bold;");

        Label savingLabel = new Label("可节省 ¥" + suggestion.getSuggestedSaving());
        savingLabel.setStyle("-fx-text-fill: green;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleRow.getChildren().addAll(categoryLabel, spacer, savingLabel);

        Label reasonLabel = new Label(suggestion.getReason());
        reasonLabel.setWrapText(true);
        reasonLabel.setTextAlignment(TextAlignment.LEFT);

        box.getChildren().addAll(titleRow, reasonLabel);
        return box;
    }

    /**
     * 应用预算建议
     */
    private void applyRecommendations() {
        if (currentRecommendation == null) return;

        showConfirmAlert("应用预算建议", "确定要将这些预算建议应用到您的预算设置中吗？", () -> {
            try {
                Map<Category, BigDecimal> recommendedBudget = currentRecommendation.generateRecommendedBudget();

                // TODO: 调用预算服务更新预算设置
                // budgetService.updateBudgets(appContext.getCurrentUser().getId(), recommendedBudget);

                showInfoAlert("成功", "预算建议已成功应用到您的预算设置中");
            } catch (Exception e) {
                showErrorAlert("应用预算建议失败", e.getMessage());
            }
        });
    }

    /**
     * 获取选择的月份数量
     */
    private int getSelectedMonthsCount() {
        String selected = timeRangeComboBox.getValue();

        return switch (selected) {
            case "最近3个月" -> 3;
            case "最近6个月" -> 6;
            case "最近12个月" -> 12;
            case "今年" -> {
                int currentMonth = LocalDate.now().getMonthValue();
                yield currentMonth;
            }
            case "去年" -> 12;
            default -> 6;
        };
    }

    /**
     * 显示无数据消息
     */
    private void showNoDataMessage() {
        spendingTrendChart.getData().clear();
        categoryDistributionChart.getData().clear();

        // 清除所有容器
        topGrowthCategoriesContainer.getChildren().clear();
        budgetRecommendationsContainer.getChildren().clear();
        savingSuggestionsContainer.getChildren().clear();

        // 显示默认值
        avgMonthlySpendingLabel.setText("¥0.00");
        spendingTrendLabel.setText("0%");

        // 显示无数据提示
        Label noDataLabel = new Label("暂无数据可供分析");
        noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        budgetRecommendationsContainer.getChildren().add(noDataLabel);
    }

    /**
     * 显示错误提示对话框
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示信息提示对话框
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 显示确认对话框
     */
    private void showConfirmAlert(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }
}