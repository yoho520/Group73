package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.SavingsTier;
import com.example.software.financeapp.model.enums.SavingsPriority;
import com.example.software.financeapp.service.ApiService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * 储蓄层级表单控制器
 */
public class SavingsTierFormController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<SavingsPriority> priorityComboBox;

    @FXML
    private Slider percentageSlider;

    @FXML
    private Label percentageLabel;

    @FXML
    private CheckBox activeCheckBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 数据
    private SavingsTier tier;
    private Long userId;

    // 回调
    private Consumer<SavingsTier> onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化优先级下拉框
        priorityComboBox.setItems(FXCollections.observableArrayList(SavingsPriority.values()));

        // 设置百分比滑块监听器
        percentageSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = newValue.intValue();
            percentageLabel.setText(value + "%");
        });

        // 默认选择中等优先级
        priorityComboBox.setValue(SavingsPriority.MEDIUM);
    }

    /**
     * 设置储蓄层级
     * @param tier 要编辑的层级
     */
    public void setTier(SavingsTier tier) {
        this.tier = tier;
        this.userId = tier.getUserId();

        // 填充表单字段
        nameField.setText(tier.getName());
        descriptionField.setText(tier.getDescription());
        priorityComboBox.setValue(tier.getPriority());

        // 设置百分比滑块值
        double percentage = tier.getAllocationPercentage().multiply(BigDecimal.valueOf(100)).doubleValue();
        percentageSlider.setValue(percentage);

        activeCheckBox.setSelected(tier.isActive());
    }

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 设置保存回调
     * @param callback 回调函数
     */
    public void setOnSaveCallback(Consumer<SavingsTier> callback) {
        this.onSaveCallback = callback;
    }

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
            // 创建或更新储蓄层级
            if (tier == null) {
                tier = new SavingsTier();
                tier.setUserId(userId);
            }

            // 设置表单数据
            tier.setName(nameField.getText().trim());
            tier.setDescription(descriptionField.getText().trim());
            tier.setPriority(priorityComboBox.getValue());

            // 计算分配比例
            double percentage = percentageSlider.getValue() / 100.0;
            tier.setAllocationPercentage(BigDecimal.valueOf(percentage));

            tier.setActive(activeCheckBox.isSelected());

            // 保存层级
            ApiService apiService = appContext.getApiService();

            SavingsTier savedTier;
            if (tier.getId() == null) {
                savedTier = apiService.createSavingsTier(tier);
            } else {
                savedTier = apiService.updateSavingsTier(tier);
            }

            // 调用回调
            if (onSaveCallback != null && savedTier != null) {
                onSaveCallback.accept(savedTier);
            }

            // 关闭对话框
            closeDialog();

        } catch (Exception e) {
            showErrorAlert("保存失败", "无法保存储蓄层级: " + e.getMessage());
        }
    }

    /**
     * 验证输入
     * @return 是否验证通过
     */
    private boolean validateInputs() {
        // 验证名称
        if (nameField.getText().trim().isEmpty()) {
            showErrorAlert("验证错误", "请输入层级名称");
            nameField.requestFocus();
            return false;
        }

        // 验证优先级
        if (priorityComboBox.getValue() == null) {
            showErrorAlert("验证错误", "请选择优先级");
            priorityComboBox.requestFocus();
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