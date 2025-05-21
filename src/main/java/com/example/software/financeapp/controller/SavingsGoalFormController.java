package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.SavingsGoal;
import com.example.software.financeapp.model.entity.SavingsTier;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.savings.SavingsPlanService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * 储蓄目标表单控制器
 */
public class SavingsGoalFormController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField targetAmountField;

    @FXML
    private TextField currentAmountField;

    @FXML
    private DatePicker targetDatePicker;

    @FXML
    private ComboBox<SavingsTier> tierComboBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 数据
    private SavingsGoal goal;
    private Long userId;

    // 回调
    private Consumer<SavingsGoal> onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置金额输入验证
        targetAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                targetAmountField.setText(oldValue);
            }
        });

        currentAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                currentAmountField.setText(oldValue);
            }
        });

        // 设置日期选择器默认值
        targetDatePicker.setValue(LocalDate.now().plusYears(1));
    }

    /**
     * 设置储蓄目标
     * @param goal 要编辑的目标
     */
    public void setGoal(SavingsGoal goal) {
        this.goal = goal;
        this.userId = goal.getUserId();

        // 填充表单字段
        nameField.setText(goal.getName());
        descriptionField.setText(goal.getDescription());
        targetAmountField.setText(goal.getTargetAmount().toString());
        currentAmountField.setText(goal.getCurrentAmount().toString());

        if (goal.getTargetDate() != null) {
            targetDatePicker.setValue(goal.getTargetDate());
        }

        // 层级选择会在setTiers方法中处理
    }

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 设置储蓄层级列表
     * @param tiers 层级列表
     */
    public void setTiers(List<SavingsTier> tiers) {
        tierComboBox.setItems(FXCollections.observableArrayList(tiers));

        // 设置单元格工厂
        tierComboBox.setCellFactory(param -> new ListCell<SavingsTier>() {
            @Override
            protected void updateItem(SavingsTier tier, boolean empty) {
                super.updateItem(tier, empty);
                if (empty || tier == null) {
                    setText(null);
                } else {
                    setText(tier.getName());
                }
            }
        });

        // 设置按钮单元格
        tierComboBox.setButtonCell(new ListCell<SavingsTier>() {
            @Override
            protected void updateItem(SavingsTier tier, boolean empty) {
                super.updateItem(tier, empty);
                if (empty || tier == null) {
                    setText(null);
                } else {
                    setText(tier.getName());
                }
            }
        });

        // 如果正在编辑目标，选择其层级
        if (goal != null && goal.getTier() != null) {
            for (SavingsTier tier : tiers) {
                if (tier.getId().equals(goal.getTier().getId())) {
                    tierComboBox.setValue(tier);
                    break;
                }
            }
        }
    }

    /**
     * 设置保存回调
     * @param callback 回调函数
     */
    public void setOnSaveCallback(Consumer<SavingsGoal> callback) {
        this.onSaveCallback = callback;
    }

    /**
     * 处理取消按钮点击
     */
    /**
     * 处理取消按钮点击
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    /**
     * 处理保存按钮点击
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateInputs()) {
            return;
        }

        try {
            // 创建或更新储蓄目标
            if (goal == null) {
                goal = new SavingsGoal();
                goal.setUserId(userId);
                goal.setCompleted(false);
            }

            // 设置表单数据
            goal.setName(nameField.getText().trim());
            goal.setDescription(descriptionField.getText().trim());
            goal.setTargetAmount(new BigDecimal(targetAmountField.getText().trim()));
            goal.setCurrentAmount(new BigDecimal(currentAmountField.getText().trim()));
            goal.setTargetDate(targetDatePicker.getValue());
            goal.setTier(tierComboBox.getValue());

            // 检查是否已完成
            if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
                goal.setCompleted(true);
            }

            // 保存目标
            ApiService apiService = appContext.getApiService();
            SavingsPlanService savingsPlanService = new SavingsPlanService(apiService);

            SavingsGoal savedGoal;
            if (goal.getId() == null) {
                savedGoal = savingsPlanService.createSavingsGoal(goal);
            } else {
                savedGoal = apiService.updateSavingsGoal(goal);
            }

            // 调用回调
            if (onSaveCallback != null && savedGoal != null) {
                onSaveCallback.accept(savedGoal);
            }

            // 关闭对话框
            closeDialog();

        } catch (Exception e) {
            showErrorAlert("保存失败", "无法保存储蓄目标: " + e.getMessage());
        }
    }

    /**
     * 验证输入
     * @return 是否验证通过
     */
    private boolean validateInputs() {
        // 验证名称
        if (nameField.getText().trim().isEmpty()) {
            showErrorAlert("验证错误", "请输入目标名称");
            nameField.requestFocus();
            return false;
        }

        // 验证目标金额
        try {
            BigDecimal targetAmount = new BigDecimal(targetAmountField.getText().trim());
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                showErrorAlert("验证错误", "目标金额必须大于0");
                targetAmountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("验证错误", "请输入有效的目标金额");
            targetAmountField.requestFocus();
            return false;
        }

        // 验证当前金额
        try {
            BigDecimal currentAmount = new BigDecimal(currentAmountField.getText().trim());
            if (currentAmount.compareTo(BigDecimal.ZERO) < 0) {
                showErrorAlert("验证错误", "当前金额不能为负数");
                currentAmountField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("验证错误", "请输入有效的当前金额");
            currentAmountField.requestFocus();
            return false;
        }

        // 验证目标日期
        if (targetDatePicker.getValue() == null) {
            showErrorAlert("验证错误", "请选择目标日期");
            targetDatePicker.requestFocus();
            return false;
        }

        if (targetDatePicker.getValue().isBefore(LocalDate.now())) {
            showErrorAlert("验证错误", "目标日期不能早于今天");
            targetDatePicker.requestFocus();
            return false;
        }

        // 验证层级
        if (tierComboBox.getValue() == null) {
            showErrorAlert("验证错误", "请选择储蓄层级");
            tierComboBox.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 关闭对话框
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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
}
