package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.localization.ChineseHolidayService;
import com.example.software.financeapp.service.localization.RegionalConsumptionService;
import com.example.software.financeapp.service.localization.RegionalConsumptionService.ConsumptionPeakPeriod;
import com.example.software.financeapp.service.localization.RegionalConsumptionService.ConsumptionAnalysis;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 本地化设置控制器
 * 负责提供城市和行业适配，以及消费特点分析
 */
public class LocalizationSettingsController implements Initializable {

    @FXML
    private ComboBox<String> cityComboBox;

    @FXML
    private ComboBox<String> industryComboBox;

    @FXML
    private TextField incomeField;

    @FXML
    private Button applyButton;

    @FXML
    private DatePicker analysisDatePicker;

    @FXML
    private Button analyzeButton;

    @FXML
    private TextFlow analysisResultText;

    @FXML
    private TableView<ConsumptionPeakPeriod> peakPeriodsTable;

    @FXML
    private TableColumn<ConsumptionPeakPeriod, String> peakNameColumn;

    @FXML
    private TableColumn<ConsumptionPeakPeriod, String> peakStartColumn;

    @FXML
    private TableColumn<ConsumptionPeakPeriod, String> peakEndColumn;

    @FXML
    private TableColumn<ConsumptionPeakPeriod, String> peakFactorColumn;

    @FXML
    private TableColumn<ConsumptionPeakPeriod, String> peakCategoriesColumn;

    @FXML
    private PieChart budgetPieChart;

    @FXML
    private BarChart<String, Number> consumptionFactorChart;

    @FXML
    private CategoryAxis factorXAxis;

    @FXML
    private NumberAxis factorYAxis;

    @FXML
    private VBox holidayInfoBox;

    @FXML
    private Label nextHolidayLabel;

    @FXML
    private Label daysUntilHolidayLabel;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 服务
    private ApiService apiService;
    private ChineseHolidayService holidayService;
    private RegionalConsumptionService regionalService;

    // 数据
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Map<String, BigDecimal> currentBudget;
    private ConsumptionAnalysis currentAnalysis;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.holidayService = new ChineseHolidayService();
        this.regionalService = new RegionalConsumptionService();

        // 初始化城市下拉框
        cityComboBox.setItems(FXCollections.observableArrayList(
                "北京", "上海", "广州", "深圳", "成都", "杭州", "武汉", "西安",
                "南京", "重庆", "天津", "苏州", "长沙", "青岛", "宁波"
        ));

        // 初始化行业下拉框
        industryComboBox.setItems(FXCollections.observableArrayList(
                "IT/互联网", "金融", "教育", "医疗", "制造业", "零售", "房地产",
                "文化/传媒", "政府/机构", "建筑", "服务业", "其他"
        ));

        // 设置收入输入验证
        incomeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                incomeField.setText(oldValue);
            }
        });

        // 初始化日期选择器
        analysisDatePicker.setValue(LocalDate.now());

        // 初始化表格列
        initializeTableColumns();

        // 更新假日信息
        updateHolidayInfo();

        // 默认选择
        cityComboBox.setValue("北京");
        industryComboBox.setValue("IT/互联网");
        incomeField.setText("10000");

        // 初始化生成预算
        handleApplySettings(null);
    }

    /**
     * 初始化表格列
     */
    private void initializeTableColumns() {
        peakNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        peakStartColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartDate().format(dateFormatter)));

        peakEndColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndDate().format(dateFormatter)));

        peakFactorColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f倍", cellData.getValue().getConsumptionFactor())));

        peakCategoriesColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.join("、", cellData.getValue().getMainCategories())));

        // 设置行点击事件
        peakPeriodsTable.setRowFactory(tv -> {
            TableRow<ConsumptionPeakPeriod> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    showPeakPeriodDetails(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * 处理应用设置按钮点击
     */
    @FXML
    private void handleApplySettings(ActionEvent event) {
        String city = cityComboBox.getValue();
        String industry = industryComboBox.getValue();

        if (city == null || industry == null || incomeField.getText().isEmpty()) {
            showErrorAlert("设置不完整", "请选择城市、行业并输入月收入");
            return;
        }

        try {
            BigDecimal income = new BigDecimal(incomeField.getText());

            // 生成个性化预算
            currentBudget = regionalService.generatePersonalizedBudget(city, industry, income);

            // 更新预算图表
            updateBudgetChart();

            // 更新消费高峰期
            updateConsumptionPeaks();

            // 分析当前日期
            LocalDate date = analysisDatePicker.getValue();
            if (date != null) {
                currentAnalysis = regionalService.analyzeConsumptionPattern(city, date);
                updateAnalysisResult();
                updateConsumptionFactorChart();
            }

            showInfoAlert("设置已应用", "已根据您的所在城市和行业生成个性化财务分析");

        } catch (NumberFormatException e) {
            showErrorAlert("输入错误", "请输入有效的收入金额");
        }
    }

    /**
     * 处理分析按钮点击
     */
    @FXML
    private void handleAnalyzeDate(ActionEvent event) {
        String city = cityComboBox.getValue();
        LocalDate date = analysisDatePicker.getValue();

        if (city == null || date == null) {
            showErrorAlert("分析失败", "请选择城市和日期");
            return;
        }

        // 分析特定日期的消费模式
        currentAnalysis = regionalService.analyzeConsumptionPattern(city, date);

        // 更新分析结果
        updateAnalysisResult();

        // 更新消费系数图表
        updateConsumptionFactorChart();
    }

    /**
     * 更新预算图表
     */
    private void updateBudgetChart() {
        budgetPieChart.getData().clear();

        if (currentBudget == null || currentBudget.isEmpty()) {
            return;
        }

        // 按金额降序排序
        List<Map.Entry<String, BigDecimal>> sortedEntries = currentBudget.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 创建饼图数据
        for (Map.Entry<String, BigDecimal> entry : sortedEntries) {
            String category = entry.getKey();
            BigDecimal amount = entry.getValue();

            PieChart.Data slice = new PieChart.Data(
                    category + " ¥" + String.format("%.2f", amount),
                    amount.doubleValue()
            );

            budgetPieChart.getData().add(slice);
        }
    }

    /**
     * 更新消费系数图表
     */
    private void updateConsumptionFactorChart() {
        consumptionFactorChart.getData().clear();

        if (currentAnalysis == null) {
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("消费系数(基准=1.0)");

        series.getData().add(new XYChart.Data<>("城市", currentAnalysis.getCityConsumptionFactor()));
        series.getData().add(new XYChart.Data<>("日期", currentAnalysis.getDateConsumptionFactor()));
        series.getData().add(new XYChart.Data<>("季节", currentAnalysis.getSeasonConsumptionFactor()));
        series.getData().add(new XYChart.Data<>("综合", currentAnalysis.getCombinedConsumptionFactor()));

        consumptionFactorChart.getData().add(series);
    }

    /**
     * 更新消费高峰期
     */
    private void updateConsumptionPeaks() {
        // 预测未来90天的消费高峰期
        List<ConsumptionPeakPeriod> peaks =
                regionalService.predictConsumptionPeaks(LocalDate.now(), 90);

        // 更新表格
        peakPeriodsTable.setItems(FXCollections.observableArrayList(peaks));
    }

    /**
     * 更新假日信息
     */
    private void updateHolidayInfo() {
        LocalDate today = LocalDate.now();
        ChineseHolidayService.Holiday nextHoliday = holidayService.getNextHoliday(today);

        if (nextHoliday != null) {
            long daysUntil = holidayService.getDaysUntilNextHoliday(today);

            nextHolidayLabel.setText("下一个节假日: " + nextHoliday.getName() +
                    " (" + nextHoliday.getStartDate().format(dateFormatter) + " 至 " +
                    nextHoliday.getEndDate().format(dateFormatter) + ")");

            daysUntilHolidayLabel.setText("距离还有 " + daysUntil + " 天");
        } else {
            nextHolidayLabel.setText("未找到即将到来的节假日信息");
            daysUntilHolidayLabel.setText("");
        }
    }
    private void updateAnalysisResult() {
        analysisResultText.getChildren().clear();

        if (currentAnalysis == null) {
            return;
        }

        // 创建分析结果文本
        Text dateText = new Text("日期: " + currentAnalysis.getDate().format(dateFormatter) +
                " (" + currentAnalysis.getDateType() + ")\n");
        dateText.setStyle("-fx-font-weight: bold;");

        Text cityText = new Text("城市: " + currentAnalysis.getCityName() +
                " (" + currentAnalysis.getCityTier() + ")\n");
        cityText.setStyle("-fx-font-weight: bold;");

        Text seasonText = new Text("季节: " + currentAnalysis.getSeason() + "\n\n");
        seasonText.setStyle("-fx-font-weight: bold;");

        Text consumptionAdviceText = new Text(currentAnalysis.getConsumptionAdvice() + "\n\n");

        // 城市特点
        Text cityFeaturesTitle = new Text("城市消费特点:\n");
        cityFeaturesTitle.setStyle("-fx-font-weight: bold;");

        StringBuilder cityFeatures = new StringBuilder();
        for (String feature : currentAnalysis.getCityFeatures()) {
            cityFeatures.append("• ").append(feature).append("\n");
        }
        Text cityFeaturesText = new Text(cityFeatures.toString() + "\n");

        // 推荐消费类别
        Text recommendedTitle = new Text("推荐关注类别:\n");
        recommendedTitle.setStyle("-fx-font-weight: bold;");

        StringBuilder recommended = new StringBuilder();
        for (String category : currentAnalysis.getRecommendedCategories()) {
            recommended.append("• ").append(category).append("\n");
        }
        Text recommendedText = new Text(recommended.toString());

        analysisResultText.getChildren().addAll(
                dateText, cityText, seasonText,
                consumptionAdviceText,
                cityFeaturesTitle, cityFeaturesText,
                recommendedTitle, recommendedText
        );
    }

    /**
     * 显示消费高峰期详情
     */
    private void showPeakPeriodDetails(ConsumptionPeakPeriod peak) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("消费高峰期详情");
        alert.setHeaderText(peak.getName());

        // 构建详情信息
        StringBuilder content = new StringBuilder();
        content.append("时间范围: ").append(peak.getStartDate().format(dateFormatter))
                .append(" 至 ").append(peak.getEndDate().format(dateFormatter))
                .append(" (共").append(peak.getDurationDays()).append("天)\n\n");

        content.append("消费系数: ").append(String.format("%.1f", peak.getConsumptionFactor()))
                .append("倍 (相对于普通日)\n\n");

        content.append("主要消费类别:\n");
        for (String category : peak.getMainCategories()) {
            content.append("• ").append(category).append("\n");
        }
        content.append("\n");

        content.append("描述:\n").append(peak.getDescription());

        alert.setContentText(content.toString());
        alert.showAndWait();
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