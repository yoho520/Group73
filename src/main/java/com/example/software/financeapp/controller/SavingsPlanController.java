package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.SavingsGoal;
import com.example.software.financeapp.model.entity.SavingsTier;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.SavingsPriority;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.savings.SavingsPlanService;
import com.example.software.financeapp.service.savings.SavingsPlanService.SavingsPlan;
import com.example.software.financeapp.service.savings.SavingsPlanService.SavingsGoalProgress;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * 智能储蓄计划控制器
 */
public class SavingsPlanController implements Initializable {

    @FXML
    private Label recommendedSavingsLabel;

    @FXML
    private Label totalGoalsLabel;

    @FXML
    private Label activeGoalsLabel;

    @FXML
    private Label completedGoalsLabel;

    @FXML
    private ListView<SavingsTier> tierListView;

    @FXML
    private PieChart tierAllocationChart;

    @FXML
    private TableView<SavingsGoal> goalsTableView;

    @FXML
    private TableColumn<SavingsGoal, String> goalNameColumn;

    @FXML
    private TableColumn<SavingsGoal, BigDecimal> goalTargetColumn;

    @FXML
    private TableColumn<SavingsGoal, BigDecimal> goalCurrentColumn;

    @FXML
    private TableColumn<SavingsGoal, String> goalProgressColumn;

    @FXML
    private TableColumn<SavingsGoal, String> goalDateColumn;

    @FXML
    private TableColumn<SavingsGoal, String> goalTierColumn;

    @FXML
    private VBox goalDetailPane;

    @FXML
    private Label selectedGoalNameLabel;

    @FXML
    private Label goalProgressDetailLabel;

    @FXML
    private Label goalCompletionDateLabel;

    @FXML
    private Label goalMonthlyAllocationLabel;

    @FXML
    private Label goalRemainingMonthsLabel;

    @FXML
    private ProgressBar goalProgressBar;

    @FXML
    private Button updateProgressButton;

    @FXML
    private Button editGoalButton;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 服务
    private ApiService apiService;
    private SavingsPlanService savingsPlanService;

    // 数据
    private ObservableList<SavingsTier> tiers = FXCollections.observableArrayList();
    private ObservableList<SavingsGoal> goals = FXCollections.observableArrayList();
    private SavingsPlan currentPlan;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.savingsPlanService = new SavingsPlanService(apiService);

        // 初始化表格列
        initializeTableColumns();

        // 初始化目标详情区域
        initializeGoalDetailsPane();

        // 设置列表视图单元格工厂
        tierListView.setCellFactory(param -> new ListCell<SavingsTier>() {
            @Override
            protected void updateItem(SavingsTier tier, boolean empty) {
                super.updateItem(tier, empty);
                if (empty || tier == null) {
                    setText(null);
                } else {
                    String text = tier.getName() + " (" + tier.getAllocationPercentage().multiply(BigDecimal.valueOf(100)) + "%)";
                    setText(text);
                }
            }
        });

        // 加载数据
        loadData();
    }

    /**
     * 初始化表格列
     */
    private void initializeTableColumns() {
        goalNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        goalTargetColumn.setCellValueFactory(new PropertyValueFactory<>("targetAmount"));
        goalTargetColumn.setCellFactory(column -> new TableCell<SavingsGoal, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("¥%.2f", amount));
                }
            }
        });

        goalCurrentColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        goalCurrentColumn.setCellFactory(column -> new TableCell<SavingsGoal, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("¥%.2f", amount));
                }
            }
        });

        goalProgressColumn.setCellValueFactory(cellData -> {
            SavingsGoal goal = cellData.getValue();
            BigDecimal percentage = goal.getCompletionPercentage();
            return new SimpleStringProperty(percentage + "%");
        });

        goalDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTargetDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "无截止日期");
        });

        goalTierColumn.setCellValueFactory(cellData -> {
            SavingsTier tier = cellData.getValue().getTier();
            return new SimpleStringProperty(tier != null ? tier.getName() : "未分配");
        });

        // 选择变更监听
        goalsTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateGoalDetails(newValue));
    }

    /**
     * 初始化目标详情区域
     */
    private void initializeGoalDetailsPane() {
        // 默认禁用编辑按钮，直到选择目标
        updateProgressButton.setDisable(true);
        editGoalButton.setDisable(true);

        // 隐藏详情面板，直到选择目标
        goalDetailPane.setVisible(false);
    }

    /**
     * 加载数据
     */
    private void loadData() {
        try {
            // 获取当前用户
            User currentUser = appContext.getCurrentUser();
            if (currentUser == null) return;

            // 获取储蓄层级
            List<SavingsTier> userTiers = savingsPlanService.getUserSavingsTiers(currentUser.getId());
            tiers.clear();
            tiers.addAll(userTiers);
            tierListView.setItems(tiers);

            // 获取储蓄目标
            List<SavingsGoal> userGoals = savingsPlanService.getUserSavingsGoals(currentUser.getId());
            goals.clear();
            goals.addAll(userGoals);
            goalsTableView.setItems(goals);

            // 生成储蓄计划
            currentPlan = savingsPlanService.generateSavingsPlan(currentUser.getId());

            // 更新UI
            updatePlanSummary();
            updateTierAllocationChart();

        } catch (Exception e) {
            showErrorAlert("加载数据失败", e.getMessage());
        }
    }

    /**
     * 更新计划摘要信息
     */
    private void updatePlanSummary() {
        if (currentPlan == null) return;

        // 更新推荐储蓄金额
        recommendedSavingsLabel.setText(String.format("¥%.2f", currentPlan.getRecommendedMonthlySavings()));

        // 更新目标统计
        int total = goals.size();
        int active = 0;
        int completed = 0;

        for (SavingsGoal goal : goals) {
            if (goal.isCompleted()) {
                completed++;
            } else {
                active++;
            }
        }

        totalGoalsLabel.setText(String.valueOf(total));
        activeGoalsLabel.setText(String.valueOf(active));
        completedGoalsLabel.setText(String.valueOf(completed));
    }

    /**
     * 更新层级分配图表
     */
    private void updateTierAllocationChart() {
        tierAllocationChart.getData().clear();

        if (currentPlan == null) return;

        // 创建饼图数据
        for (Map.Entry<SavingsTier, BigDecimal> entry : currentPlan.getTierAllocations().entrySet()) {
            SavingsTier tier = entry.getKey();
            BigDecimal amount = entry.getValue();

            String label = tier.getName() + " ¥" + String.format("%.2f", amount);
            PieChart.Data slice = new PieChart.Data(label, amount.doubleValue());
            tierAllocationChart.getData().add(slice);
        }
    }

    /**
     * 更新目标详情
     * @param goal 选中的储蓄目标
     */
    private void updateGoalDetails(SavingsGoal goal) {
        if (goal == null) {
            goalDetailPane.setVisible(false);
            updateProgressButton.setDisable(true);
            editGoalButton.setDisable(true);
            return;
        }

        goalDetailPane.setVisible(true);
        updateProgressButton.setDisable(false);
        editGoalButton.setDisable(false);

        // 更新目标名称
        selectedGoalNameLabel.setText(goal.getName());

        // 更新进度信息
        BigDecimal progress = goal.getCompletionPercentage();
        goalProgressDetailLabel.setText(progress + "%");
        goalProgressBar.setProgress(progress.doubleValue() / 100);

        // 获取目标进度信息
        if (currentPlan != null && currentPlan.getGoalProgress().containsKey(goal)) {
            SavingsGoalProgress goalProgress = currentPlan.getGoalProgress().get(goal);

            // 更新预计完成日期
            LocalDate completionDate = goalProgress.getExpectedCompletionDate();
            goalCompletionDateLabel.setText(completionDate.format(dateFormatter));

            // 更新剩余月数
            long remainingMonths = goalProgress.getMonthsToCompletion();
            goalRemainingMonthsLabel.setText(String.valueOf(remainingMonths));

            // 更新每月分配
            BigDecimal monthlyAllocation = BigDecimal.ZERO;
            if (goal.getTier() != null && currentPlan.getTierAllocations().containsKey(goal.getTier())) {
                // 假设目标平均分配层级资金
                int tierGoalsCount = 0;
                for (SavingsGoal g : goals) {
                    if (g.getTier() != null && g.getTier().equals(goal.getTier()) && !g.isCompleted()) {
                        tierGoalsCount++;
                    }
                }
                if (tierGoalsCount > 0) {
                    BigDecimal tierAmount = currentPlan.getTierAllocations().get(goal.getTier());
                    monthlyAllocation = tierAmount.divide(BigDecimal.valueOf(tierGoalsCount), 2, BigDecimal.ROUND_HALF_UP);
                }
            }
            goalMonthlyAllocationLabel.setText(String.format("¥%.2f", monthlyAllocation));
        } else {
            goalCompletionDateLabel.setText("未知");
            goalRemainingMonthsLabel.setText("未知");
            goalMonthlyAllocationLabel.setText("¥0.00");
        }
    }

    /**
     * 处理刷新计划按钮点击
     */
    @FXML
    private void handleRefreshPlan(ActionEvent event) {
        loadData();
        showInfoAlert("刷新成功", "储蓄计划已更新");
    }

    /**
     * 处理新建目标按钮点击
     */
    @FXML
    private void handleNewGoal(ActionEvent event) {
        showSavingsGoalDialog(null);
    }

    /**
     * 处理添加层级按钮点击
     */
    @FXML
    private void handleAddTier(ActionEvent event) {
        showSavingsTierDialog(null);
    }

    /**
     * 处理编辑层级按钮点击
     */
    @FXML
    private void handleEditTier(ActionEvent event) {
        SavingsTier selectedTier = tierListView.getSelectionModel().getSelectedItem();
        if (selectedTier != null) {
            showSavingsTierDialog(selectedTier);
        } else {
            showInfoAlert("请选择层级", "请先选择要编辑的储蓄层级");
        }
    }

    /**
     * 处理更新目标进度按钮点击
     */
    @FXML
    private void handleUpdateGoalProgress(ActionEvent event) {
        SavingsGoal selectedGoal = goalsTableView.getSelectionModel().getSelectedItem();
        if (selectedGoal != null) {
            showUpdateProgressDialog(selectedGoal);
        }
    }

    /**
     * 处理编辑目标按钮点击
     */
    @FXML
    private void handleEditGoal(ActionEvent event) {
        SavingsGoal selectedGoal = goalsTableView.getSelectionModel().getSelectedItem();
        if (selectedGoal != null) {
            showSavingsGoalDialog(selectedGoal);
        }
    }

    /**
     * 显示储蓄目标对话框
     * @param goal 要编辑的目标，如果为null则创建新目标
     */
    private void showSavingsGoalDialog(SavingsGoal goal) {
        try {
            // 加载对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/savings_goal_form.fxml"));
            Parent root = loader.load();

            // 获取控制器
            SavingsGoalFormController controller = loader.getController();

            // 设置数据
            controller.setTiers(tiers);

            if (goal != null) {
                controller.setGoal(goal);
            } else {
                // 创建新目标时，设置当前用户ID
                User currentUser = appContext.getCurrentUser();
                if (currentUser != null) {
                    controller.setUserId(currentUser.getId());
                }
            }

            // 设置保存回调
            controller.setOnSaveCallback(savedGoal -> {
                // 刷新数据
                loadData();
            });

            // 创建对话框
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(appContext.getPrimaryStage());
            dialog.setTitle(goal == null ? "新建储蓄目标" : "编辑储蓄目标");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showErrorAlert("加载表单失败", e.getMessage());
        }
    }

    /**
     * 显示储蓄层级对话框
     * @param tier 要编辑的层级，如果为null则创建新层级
     */
    private void showSavingsTierDialog(SavingsTier tier) {
        try {
            // 加载对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/savings_tier_form.fxml"));
            Parent root = loader.load();

            // 获取控制器
            SavingsTierFormController controller = loader.getController();

            if (tier != null) {
                controller.setTier(tier);
            } else {
                // 创建新层级时，设置当前用户ID
                User currentUser = appContext.getCurrentUser();
                if (currentUser != null) {
                    controller.setUserId(currentUser.getId());
                }
            }

            // 设置保存回调
            controller.setOnSaveCallback(savedTier -> {
                // 刷新数据
                loadData();
            });

            // 创建对话框
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(appContext.getPrimaryStage());
            dialog.setTitle(tier == null ? "新建储蓄层级" : "编辑储蓄层级");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showErrorAlert("加载表单失败", e.getMessage());
        }
    }

    /**
     * 显示更新进度对话框
     * @param goal 要更新的目标
     */
    private void showUpdateProgressDialog(SavingsGoal goal) {
        // 创建文本输入对话框
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("更新储蓄进度");
        dialog.setHeaderText("为\"" + goal.getName() + "\"添加新的储蓄金额");
        dialog.setContentText("金额(¥):");

        // 添加验证器
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    new BigDecimal(newValue);
                }
            } catch (NumberFormatException e) {
                dialog.getEditor().setText(oldValue);
            }
        });

        // 显示对话框并等待用户响应
        Optional<String> result = dialog.showAndWait();

        // 处理结果
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showErrorAlert("金额无效", "请输入大于0的金额");
                    return;
                }

                // 更新目标进度
                SavingsGoal updatedGoal = savingsPlanService.updateSavingsGoalProgress(goal.getId(), amount);

                if (updatedGoal != null) {
                    // 刷新数据
                    loadData();

                    // 如果目标已经完成，显示祝贺信息
                    if (updatedGoal.isCompleted() && !goal.isCompleted()) {
                        showInfoAlert("恭喜!", "您已经完成了\"" + goal.getName() + "\"储蓄目标!");
                    } else {
                        showInfoAlert("进度已更新", "已为\"" + goal.getName() + "\"添加¥" + amount + "的储蓄金额");
                    }
                }
            } catch (NumberFormatException e) {
                showErrorAlert("格式错误", "请输入有效的金额");
            }
        });
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