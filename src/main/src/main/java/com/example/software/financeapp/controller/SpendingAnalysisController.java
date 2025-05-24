package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.analysis.ExpenditureAnalysisService;
import com.example.software.financeapp.util.DateUtil;
import com.example.software.financeapp.util.FileUtil;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.ResourceBundle;

/**
 * 支出分析控制器 - 提供详细的支出趋势和分布分析
 */
public class SpendingAnalysisController implements Initializable {

    // 日期选择控件
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button applyButton;

    // 统计指标标签
    @FXML
    private Label totalSpendingLabel;

    @FXML
    private Label avgMonthlySpendingLabel;

    @FXML
    private Label maxSpendingLabel;

    @FXML
    private Label spendingFrequencyLabel;

    // 支出趋势相关控件
    @FXML
    private ComboBox<String> trendGranularityComboBox;

    @FXML
    private ComboBox<String> trendChartTypeComboBox;

    @FXML
    private LineChart<String, Number> spendingTrendLineChart;

    @FXML
    private BarChart<String, Number> spendingTrendBarChart;

    @FXML
    private CategoryAxis trendXAxis;

    @FXML
    private NumberAxis trendYAxis;

    // 类别分布相关控件
    @FXML
    private ComboBox<String> distributionChartTypeComboBox;

    @FXML
    private PieChart categoryPieChart;

    @FXML
    private BarChart<String, Number> categoryBarChart;

    // 热图相关控件
    @FXML
    private ComboBox<String> heatmapGranularityComboBox;

    @FXML
    private StackPane heatmapContainer;

    // 消费模式洞察容器
    @FXML
    private VBox patternInsightsContainer;

    // 服务和应用程序上下文
    private final AppContext appContext = AppContext.getInstance();
    private ApiService apiService;
    private ExpenditureAnalysisService analysisService;

    // 数据相关字段
    private List<Transaction> allTransactions;
    private List<Transaction> filteredTransactions;
    private Map<Category, BigDecimal> categoryDistribution;
    private Map<String, BigDecimal> timeSeriesData;
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("yyyy-'W'w");
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.analysisService = new ExpenditureAnalysisService();

        // 初始化日期选择器
        initializeDatePickers();

        // 初始化图表控制下拉框
        initializeComboBoxes();

        // 设置图表切换监听器
        setupChartTypeListeners();

        // 加载初始数据
        loadData();
    }

    /**
     * 初始化日期选择器
     */
    private void initializeDatePickers() {
        // 设置默认日期范围为最近3个月
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(3));

        // 添加日期验证
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null &&
                    newVal.isAfter(endDatePicker.getValue())) {
                startDatePicker.setValue(oldVal);
            }
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null &&
                    newVal.isBefore(startDatePicker.getValue())) {
                endDatePicker.setValue(oldVal);
            }
        });
    }

    /**
     * 初始化下拉框
     */
    private void initializeComboBoxes() {
        // 趋势图粒度
        trendGranularityComboBox.setItems(FXCollections.observableArrayList(
                "日", "周", "月"
        ));
        trendGranularityComboBox.setValue("月");
        trendGranularityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTrendChart());

        // 趋势图类型
        trendChartTypeComboBox.setItems(FXCollections.observableArrayList(
                "折线图", "柱状图"
        ));
        trendChartTypeComboBox.setValue("折线图");

        // 类别分布图类型
        distributionChartTypeComboBox.setItems(FXCollections.observableArrayList(
                "饼图", "柱状图"
        ));
        distributionChartTypeComboBox.setValue("饼图");

        // 热图粒度
        heatmapGranularityComboBox.setItems(FXCollections.observableArrayList(
                "日", "周", "月"
        ));
        heatmapGranularityComboBox.setValue("日");
        heatmapGranularityComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateHeatmap());
    }

    /**
     * 设置图表类型切换监听器
     */
    private void setupChartTypeListeners() {
        // 趋势图类型切换
        trendChartTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("折线图".equals(newVal)) {
                spendingTrendLineChart.setVisible(true);
                spendingTrendBarChart.setVisible(false);
            } else {
                spendingTrendLineChart.setVisible(false);
                spendingTrendBarChart.setVisible(true);
            }
            updateTrendChart();
        });

        // 分布图类型切换
        distributionChartTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("饼图".equals(newVal)) {
                categoryPieChart.setVisible(true);
                categoryBarChart.setVisible(false);
            } else {
                categoryPieChart.setVisible(false);
                categoryBarChart.setVisible(true);
            }
            updateDistributionChart();
        });
    }

    /**
     * 加载数据
     */
    @FXML
    private void loadData() {
        try {
            // 获取当前用户
            User currentUser = appContext.getCurrentUser();
            if (currentUser == null) return;

            // 获取所有交易记录
            allTransactions = apiService.getTransactions(currentUser.getId(), 0, 1000);

            // 应用日期过滤
            applyDateFilter();

        } catch (IOException e) {
            showErrorAlert("加载数据失败", e.getMessage());
        }
    }

    /**
     * 应用日期过滤
     */
    private void applyDateFilter() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) return;

        // 过滤交易记录
        filteredTransactions = allTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> {
                    LocalDate date = t.getTransactionDate().toLocalDate();
                    return !date.isBefore(startDate) && !date.isAfter(endDate);
                })
                .collect(Collectors.toList());

        // 更新统计和图表
        updateStatistics();
        updateTrendChart();
        updateDistributionChart();
        updateHeatmap();
        updatePatternInsights();
    }

    /**
     * 处理应用过滤按钮点击
     */
    @FXML
    private void handleApplyFilter(ActionEvent event) {
        applyDateFilter();
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        if (filteredTransactions.isEmpty()) {
            totalSpendingLabel.setText("¥0.00");
            avgMonthlySpendingLabel.setText("¥0.00");
            maxSpendingLabel.setText("¥0.00");
            spendingFrequencyLabel.setText("0次/月");
            return;
        }

        // 计算总支出
        BigDecimal totalSpending = filteredTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalSpendingLabel.setText(String.format("¥%.2f", totalSpending));

        // 计算月均支出
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        long months = ChronoUnit.MONTHS.between(startDate, endDate) + 1;
        if (months < 1) months = 1;

        BigDecimal avgMonthlySpending = totalSpending.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        avgMonthlySpendingLabel.setText(String.format("¥%.2f", avgMonthlySpending));

        // 计算最高单笔支出
        BigDecimal maxSpending = filteredTransactions.stream()
                .map(Transaction::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        maxSpendingLabel.setText(String.format("¥%.2f", maxSpending));

        // 计算支出频率
        double frequency = (double) filteredTransactions.size() / months;
        spendingFrequencyLabel.setText(String.format("%.1f次/月", frequency));
    }

    /**
     * 更新趋势图表
     */
    private void updateTrendChart() {
        if (filteredTransactions == null || filteredTransactions.isEmpty()) {
            spendingTrendLineChart.getData().clear();
            spendingTrendBarChart.getData().clear();
            return;
        }

        // 获取时间粒度
        String granularity = trendGranularityComboBox.getValue();

        // 准备时间序列数据
        timeSeriesData = new LinkedHashMap<>();

        // 根据不同粒度分组数据
        switch (granularity) {
            case "日":
                prepareTimeSeriesDataByDay();
                break;
            case "周":
                prepareTimeSeriesDataByWeek();
                break;
            case "月":
            default:
                prepareTimeSeriesDataByMonth();
                break;
        }

        // 更新图表
        if ("折线图".equals(trendChartTypeComboBox.getValue())) {
            updateLineChart();
        } else {
            updateBarChart();
        }
    }

    /**
     * 准备按日分组的时间序列数据
     */
    private void prepareTimeSeriesDataByDay() {
        // 获取日期范围
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 初始化每一天的数据
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            timeSeriesData.put(date.format(dayFormatter), BigDecimal.ZERO);
        }

        // 按日汇总
        for (Transaction t : filteredTransactions) {
            LocalDate date = t.getTransactionDate().toLocalDate();
            String key = date.format(dayFormatter);

            if (timeSeriesData.containsKey(key)) {
                BigDecimal current = timeSeriesData.get(key);
                timeSeriesData.put(key, current.add(t.getAmount()));
            }
        }
    }

    /**
     * 准备按周分组的时间序列数据
     */
    private void prepareTimeSeriesDataByWeek() {
        // 获取日期范围
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 调整到周的开始
        LocalDate startWeek = startDate.with(DayOfWeek.MONDAY);

        // 初始化每周的数据
        for (LocalDate date = startWeek; !date.isAfter(endDate); date = date.plusWeeks(1)) {
            timeSeriesData.put(date.format(weekFormatter), BigDecimal.ZERO);
        }

        // 按周汇总
        for (Transaction t : filteredTransactions) {
            LocalDate date = t.getTransactionDate().toLocalDate();
            // 获取该日期所在周的周一
            LocalDate weekStart = date.with(DayOfWeek.MONDAY);
            String key = weekStart.format(weekFormatter);

            if (timeSeriesData.containsKey(key)) {
                BigDecimal current = timeSeriesData.get(key);
                timeSeriesData.put(key, current.add(t.getAmount()));
            }
        }
    }

    /**
     * 准备按月分组的时间序列数据
     */
    private void prepareTimeSeriesDataByMonth() {
        // 获取日期范围
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // 调整到月的开始
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        // 初始化每个月的数据
        for (YearMonth month = startMonth; !month.isAfter(endMonth); month = month.plusMonths(1)) {
            timeSeriesData.put(month.format(monthFormatter), BigDecimal.ZERO);
        }

        // 按月汇总
        for (Transaction t : filteredTransactions) {
            YearMonth month = YearMonth.from(t.getTransactionDate());
            String key = month.format(monthFormatter);

            if (timeSeriesData.containsKey(key)) {
                BigDecimal current = timeSeriesData.get(key);
                timeSeriesData.put(key, current.add(t.getAmount()));
            }
        }
    }

    /**
     * 更新折线图
     */
    private void updateLineChart() {
        spendingTrendLineChart.getData().clear();

        // 创建数据系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("支出趋势");

        // 添加数据
        for (Map.Entry<String, BigDecimal> entry : timeSeriesData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        spendingTrendLineChart.getData().add(series);

        // 调整X轴标签
        trendXAxis.getCategories().clear();
        trendXAxis.setCategories(FXCollections.observableArrayList(timeSeriesData.keySet()));
    }

    /**
     * 更新柱状图
     */
    private void updateBarChart() {
        spendingTrendBarChart.getData().clear();

        // 创建数据系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("支出趋势");

        // 添加数据
        for (Map.Entry<String, BigDecimal> entry : timeSeriesData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        spendingTrendBarChart.getData().add(series);
    }

    /**
     * 更新分布图表
     */
    private void updateDistributionChart() {
        if (filteredTransactions == null || filteredTransactions.isEmpty()) {
            categoryPieChart.getData().clear();
            categoryBarChart.getData().clear();
            return;
        }

        // 计算类别分布
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        categoryDistribution = analysisService.calculateCategoryDistribution(filteredTransactions, startDate, endDate);

        if ("饼图".equals(distributionChartTypeComboBox.getValue())) {
            updatePieChart();
        } else {
            updateCategoryBarChart();
        }
    }

    /**
     * 更新饼图
     */
    private void updatePieChart() {
        categoryPieChart.getData().clear();

        // 创建饼图数据
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<Category, BigDecimal> entry : categoryDistribution.entrySet()) {
            Category category = entry.getKey();
            BigDecimal amount = entry.getValue();

            pieChartData.add(new PieChart.Data(category.getName(), amount.doubleValue()));
        }

        categoryPieChart.setData(pieChartData);

        // 添加金额标签
        pieChartData.forEach(data -> {
            data.getNode().setOnMouseEntered(event -> {
                String text = String.format("%s: ¥%.2f", data.getName(), data.getPieValue());
                Tooltip tooltip = new Tooltip(text);
                Tooltip.install(data.getNode(), tooltip);
            });
        });
    }

    /**
     * 更新类别柱状图
     */
    private void updateCategoryBarChart() {
        categoryBarChart.getData().clear();

        // 创建数据系列
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("类别支出");

        // 按金额降序排序
        List<Map.Entry<Category, BigDecimal>> sortedEntries = categoryDistribution.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 添加数据
        for (Map.Entry<Category, BigDecimal> entry : sortedEntries) {
            series.getData().add(new XYChart.Data<>(entry.getKey().getName(), entry.getValue()));
        }

        categoryBarChart.getData().add(series);
    }

    /**
     * 更新热图
     */
    private void updateHeatmap() {
        if (filteredTransactions == null || filteredTransactions.isEmpty()) {
            heatmapContainer.getChildren().clear();
            return;
        }

        String granularity = heatmapGranularityComboBox.getValue();

        if ("日".equals(granularity)) {
            createDailyHeatmap();
        } else if ("周".equals(granularity)) {
            createWeeklyHeatmap();
        } else {
            createMonthlyHeatmap();
        }
    }

    /**
     * 创建日粒度热图（按一周7天和24小时）
     */
    private void createDailyHeatmap() {
        heatmapContainer.getChildren().clear();

        // 创建24x7的二维数组，表示一周内每天每小时的支出
        BigDecimal[][] heatmapData = new BigDecimal[7][24];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 24; j++) {
                heatmapData[i][j] = BigDecimal.ZERO;
            }
        }

        // 统计数据
        for (Transaction t : filteredTransactions) {
            LocalDateTime dateTime = t.getTransactionDate();
            int dayOfWeek = dateTime.getDayOfWeek().getValue() - 1; // 0-6
            int hour = dateTime.getHour(); // 0-23

            heatmapData[dayOfWeek][hour] = heatmapData[dayOfWeek][hour].add(t.getAmount());
        }

        // 找出最大值，用于颜色比例尺
        BigDecimal maxValue = Arrays.stream(heatmapData)
                .flatMap(Arrays::stream)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);

        // 创建热图
        Pane heatmap = new Pane();
        heatmap.setPrefSize(800, 400);

        // 单元格大小
        double cellWidth = 800.0 / 24;
        double cellHeight = 300.0 / 7;

        // 绘制热图单元格
        for (int day = 0; day < 7; day++) {
            for (int hour = 0; hour < 24; hour++) {
                BigDecimal value = heatmapData[day][hour];
                double intensity = value.doubleValue() / maxValue.doubleValue();

                Rectangle cell = new Rectangle(
                        hour * cellWidth,
                        day * cellHeight,
                        cellWidth,
                        cellHeight
                );

                // 计算颜色强度（使用从白色到红色的渐变）
                Color color = Color.rgb(
                        255,
                        (int) (255 * (1 - intensity)),
                        (int) (255 * (1 - intensity))
                );

                cell.setFill(color);
                cell.setStroke(Color.LIGHTGRAY);

                // 添加工具提示
                String dayName = DayOfWeek.of(day + 1).name();
                String tooltipText = String.format(
                        "%s %02d:00-%02d:00: ¥%.2f",
                        dayName, hour, hour + 1, value
                );
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(cell, tooltip);

                heatmap.getChildren().add(cell);
            }
        }

        // 添加坐标轴标签
        // Y轴（星期）
        String[] dayLabels = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 0; i < 7; i++) {
            Label label = new Label(dayLabels[i]);
            label.setLayoutX(5);
            label.setLayoutY(i * cellHeight + cellHeight/2 - 10);
            heatmap.getChildren().add(label);
        }

        // X轴（小时）
        for (int i = 0; i < 24; i += 3) {
            Label label = new Label(String.format("%02d", i));
            label.setLayoutX(i * cellWidth + cellWidth/2 - 10);
            label.setLayoutY(7 * cellHeight + 5);
            heatmap.getChildren().add(label);
        }

        heatmapContainer.getChildren().add(heatmap);
    }

    /**
     * 创建周粒度热图
     */
    private void createWeeklyHeatmap() {
        // 类似实现，但按月份和星期划分
        heatmapContainer.getChildren().clear();

        // 创建一个简单的提示标签，表示该功能正在开发中
        Label label = new Label("周粒度热图功能正在开发中...");
        label.setFont(new Font(16));
        heatmapContainer.getChildren().add(label);
    }

    /**
     * 创建月粒度热图
     */
    private void createMonthlyHeatmap() {
        // 类似实现，但按年和月划分
        heatmapContainer.getChildren().clear();

        // 创建一个简单的提示标签，表示该功能正在开发中
        Label label = new Label("月粒度热图功能正在开发中...");
        label.setFont(new Font(16));
        heatmapContainer.getChildren().add(label);
    }

    /**
     * 更新消费模式洞察
     */
    private void updatePatternInsights() {
        patternInsightsContainer.getChildren().clear();

        if (filteredTransactions == null || filteredTransactions.isEmpty()) {
            Label emptyLabel = new Label("没有足够的交易数据来分析消费模式。");
            emptyLabel.setFont(new Font(14));
            patternInsightsContainer.getChildren().add(emptyLabel);
            return;
        }

        // 1. 添加主要消费类别洞察
        addCategoryInsight();

        // 2. 添加支出时间模式洞察
        addTimePatternInsight();

        // 3. 添加周期性支出洞察
        addRecurringInsight();

        // 4. 添加异常支出洞察
        addAnomalyInsight();
    }

    /**
     * 添加类别洞察
     */
    private void addCategoryInsight() {
        VBox insightBox = createInsightBox("主要支出类别");

        // 按金额排序类别
        List<Map.Entry<Category, BigDecimal>> sortedCategories = categoryDistribution.entrySet().stream()
                .sorted(Map.Entry.<Category, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 计算总支出
        BigDecimal totalSpending = categoryDistribution.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 构建洞察文本
        StringBuilder sb = new StringBuilder();
        sb.append("您的前三大支出类别是：\n\n");

        for (int i = 0; i < sortedCategories.size(); i++) {
            Map.Entry<Category, BigDecimal> entry = sortedCategories.get(i);
            Category category = entry.getKey();
            BigDecimal amount = entry.getValue();
            BigDecimal percentage = amount.divide(totalSpending, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            sb.append(String.format("%d. %s: ¥%.2f (%.1f%%)\n",
                    i + 1, category.getName(), amount, percentage));
        }

        // 添加建议
        BigDecimal topPercentage = sortedCategories.get(0).getValue()
                .divide(totalSpending, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (topPercentage.compareTo(BigDecimal.valueOf(50)) > 0) {
            sb.append("\n您的支出过于集中在").append(sortedCategories.get(0).getKey().getName())
                    .append("类别，建议适当平衡各类支出。");
        }

        Label contentLabel = new Label(sb.toString());
        contentLabel.setWrapText(true);

        insightBox.getChildren().add(contentLabel);
        patternInsightsContainer.getChildren().add(insightBox);
    }

    /**
     * 添加时间模式洞察
     */
    private void addTimePatternInsight() {
        VBox insightBox = createInsightBox("支出时间模式");

        // 按星期统计
        Map<DayOfWeek, BigDecimal> spendingByDayOfWeek = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            spendingByDayOfWeek.put(day, BigDecimal.ZERO);
        }

        // 按小时统计
        Map<Integer, BigDecimal> spendingByHour = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            spendingByHour.put(i, BigDecimal.ZERO);
        }

        // 统计数据
        for (Transaction t : filteredTransactions) {
            LocalDateTime dateTime = t.getTransactionDate();
            DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
            int hour = dateTime.getHour();

            spendingByDayOfWeek.put(dayOfWeek,
                    spendingByDayOfWeek.get(dayOfWeek).add(t.getAmount()));

            spendingByHour.put(hour,
                    spendingByHour.get(hour).add(t.getAmount()));
        }

        // 找出支出最高的星期
        Map.Entry<DayOfWeek, BigDecimal> maxDayEntry = spendingByDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // 找出支出最高的时段
        Map.Entry<Integer, BigDecimal> maxHourEntry = spendingByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // 构建洞察文本
        StringBuilder sb = new StringBuilder();

        if (maxDayEntry != null) {
            String dayName = maxDayEntry.getKey().getDisplayName(java.time.format.TextStyle.FULL, Locale.CHINA);
            sb.append(String.format("您在%s的支出最多，占总支出的%.1f%%。\n\n",
                    dayName,
                    maxDayEntry.getValue()
                            .divide(filteredTransactions.stream()
                                    .map(Transaction::getAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))));
        }

        if (maxHourEntry != null) {
            int hour = maxHourEntry.getKey();
            sb.append(String.format("您在%d:00-%d:00时段的支出最多，这可能与您的用餐或购物习惯有关。",
                    hour, hour + 1));
        }

        Label contentLabel = new Label(sb.toString());
        contentLabel.setWrapText(true);

        insightBox.getChildren().add(contentLabel);
        patternInsightsContainer.getChildren().add(insightBox);
    }

    /**
     * 添加周期性支出洞察
     */
    private void addRecurringInsight() {
        VBox insightBox = createInsightBox("周期性支出");

        // 假设我们有一个方法来检测周期性支出
        // 这里简化处理，尝试根据名称识别一些常见的订阅服务
        List<Transaction> potentialRecurring = filteredTransactions.stream()
                .filter(t -> {
                    String description = t.getDescription().toLowerCase();
                    String merchant = t.getMerchant().toLowerCase();
                    return description.contains("会员") ||
                            description.contains("订阅") ||
                            description.contains("vip") ||
                            merchant.contains("会员") ||
                            merchant.contains("订阅") ||
                            merchant.contains("netflix") ||
                            merchant.contains("spotify") ||
                            merchant.contains("爱奇艺") ||
                            merchant.contains("腾讯视频");
                })
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        if (potentialRecurring.isEmpty()) {
            sb.append("未检测到明显的周期性订阅支出。");
        } else {
            BigDecimal totalRecurring = potentialRecurring.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            sb.append(String.format("检测到可能的订阅服务支出约¥%.2f，包括：\n\n", totalRecurring));

            for (Transaction t : potentialRecurring) {
                sb.append(String.format("• %s: ¥%.2f\n",
                        t.getMerchant(), t.getAmount()));
            }

            sb.append("\n定期审查您的订阅服务，取消不再使用的服务可以节省开支。");
        }

        Label contentLabel = new Label(sb.toString());
        contentLabel.setWrapText(true);

        insightBox.getChildren().add(contentLabel);
        patternInsightsContainer.getChildren().add(insightBox);
    }

    /**
     * 添加异常支出洞察
     */
    private void addAnomalyInsight() {
        VBox insightBox = createInsightBox("异常支出检测");

        // 计算每个类别的均值和标准差
        Map<Category, List<BigDecimal>> amountsByCategory = new HashMap<>();

        for (Transaction t : filteredTransactions) {
            Category category = t.getCategory();
            if (category != null) {
                amountsByCategory.computeIfAbsent(category, k -> new ArrayList<>())
                        .add(t.getAmount());
            }
        }

        List<Transaction> anomalies = new ArrayList<>();

        for (Map.Entry<Category, List<BigDecimal>> entry : amountsByCategory.entrySet()) {
            Category category = entry.getKey();
            List<BigDecimal> amounts = entry.getValue();

            // 只分析有足够样本的类别
            if (amounts.size() < 3) continue;

            // 计算均值
            BigDecimal sum = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mean = sum.divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

            // 计算标准差
            double variance = amounts.stream()
                    .mapToDouble(a -> Math.pow(a.subtract(mean).doubleValue(), 2))
                    .average()
                    .orElse(0);
            double stdDev = Math.sqrt(variance);

            // 查找超过均值+2倍标准差的交易
            BigDecimal threshold = mean.add(BigDecimal.valueOf(2 * stdDev));

            for (Transaction t : filteredTransactions) {
                if (category.equals(t.getCategory()) && t.getAmount().compareTo(threshold) > 0) {
                    anomalies.add(t);
                }
            }
        }

        // 只保留前5个异常
        anomalies = anomalies.stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();

        if (anomalies.isEmpty()) {
            sb.append("未检测到明显的异常支出。");
        } else {
            sb.append("检测到以下可能的异常支出：\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Transaction t : anomalies) {
                sb.append(String.format("• %s - %s: ¥%.2f（超出该类别平均水平）\n",
                        t.getTransactionDate().format(formatter),
                        t.getMerchant(),
                        t.getAmount()));
            }
        }

        Label contentLabel = new Label(sb.toString());
        contentLabel.setWrapText(true);

        insightBox.getChildren().add(contentLabel);
        patternInsightsContainer.getChildren().add(insightBox);
    }

    /**
     * 创建洞察卡片框架
     */
    private VBox createInsightBox(String title) {
        VBox box = new VBox();
        box.setSpacing(10);
        box.setPadding(new Insets(15));
        box.getStyleClass().add("insight-card");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        box.getChildren().add(titleLabel);

        return box;
    }

    /**
     * 处理导出趋势数据
     */
    @FXML
    private void handleExportTrendData(ActionEvent event) {
        if (timeSeriesData == null || timeSeriesData.isEmpty()) {
            showInfoAlert("导出", "没有可导出的数据");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出支出趋势数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv"));
        fileChooser.setInitialFileName("支出趋势数据.csv");

        File file = fileChooser.showSaveDialog(appContext.getPrimaryStage());
        if (file != null) {
            try {
                // 构建CSV内容
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("日期,金额\n");

                for (Map.Entry<String, BigDecimal> entry : timeSeriesData.entrySet()) {
                    csvContent.append(String.format("%s,%.2f\n",
                            entry.getKey(), entry.getValue()));
                }

                // 写入文件
                FileUtil.writeStringToFile(file, csvContent.toString());

                showInfoAlert("导出成功", "数据已成功导出到：" + file.getAbsolutePath());
            } catch (IOException e) {
                showErrorAlert("导出失败", e.getMessage());
            }
        }
    }

    /**
     * 处理导出分布数据
     */
    @FXML
    private void handleExportDistributionData(ActionEvent event) {
        if (categoryDistribution == null || categoryDistribution.isEmpty()) {
            showInfoAlert("导出", "没有可导出的数据");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出类别分布数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv"));
        fileChooser.setInitialFileName("类别分布数据.csv");

        File file = fileChooser.showSaveDialog(appContext.getPrimaryStage());
        if (file != null) {
            try {
                // 构建CSV内容
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("类别,金额\n");

                for (Map.Entry<Category, BigDecimal> entry : categoryDistribution.entrySet()) {
                    csvContent.append(String.format("%s,%.2f\n",
                            entry.getKey().getName(), entry.getValue()));
                }

                // 写入文件
                FileUtil.writeStringToFile(file, csvContent.toString());

                showInfoAlert("导出成功", "数据已成功导出到：" + file.getAbsolutePath());
            } catch (IOException e) {
                showErrorAlert("导出失败", e.getMessage());
            }
        }
    }

    /**
     * 处理导出热图数据
     */
    @FXML
    private void handleExportHeatmapData(ActionEvent event) {
        showInfoAlert("功能未完成", "热图数据导出功能正在开发中");
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
}