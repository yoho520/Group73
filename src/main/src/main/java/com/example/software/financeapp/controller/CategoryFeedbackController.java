package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.enums.CategoryType;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.ai.ClassificationService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 类别反馈对话框控制器
 */
public class CategoryFeedbackController implements Initializable {

    @FXML
    private Label transactionDescriptionLabel;

    @FXML
    private Label currentCategoryLabel;

    @FXML
    private Label confidenceLabel;

    @FXML
    private Label reasonLabel;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private CheckBox confirmCheckBox;

    @FXML
    private VBox confidenceContainer;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // 当前交易
    private Transaction transaction;

    // 回调函数
    private Consumer<Transaction> onSaveCallback;

    // API服务
    private ApiService apiService;

    // 分类服务
    private ClassificationService classificationService;

    // 应用上下文
    private final AppContext appContext = AppContext.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.classificationService = appContext.getClassificationService();

        // 初始化类别下拉框
        initCategoryComboBox();

        // 初始化确认复选框
        confirmCheckBox.setSelected(true);
    }

    /**
     * 初始化类别下拉框
     */
    private void initCategoryComboBox() {
        categoryComboBox.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    /**
     * 设置要编辑的交易
     * @param transaction 交易
     * @param categories 可用类别
     */
    public void setTransaction(Transaction transaction, List<Category> categories) {
        this.transaction = transaction;

        // 设置交易描述
        String description = transaction.getDescription();
        if (description == null || description.isEmpty()) {
            description = transaction.getMerchant();
        } else if (transaction.getMerchant() != null && !transaction.getMerchant().isEmpty()) {
            description = transaction.getMerchant() + " - " + description;
        }
        transactionDescriptionLabel.setText(description);

        // 设置当前类别
        String categoryName = transaction.getCategory() != null ?
                transaction.getCategory().getName() : "未分类";
        currentCategoryLabel.setText(categoryName);

        // 设置AI置信度信息
        double confidence = classificationService.getClassificationConfidence(transaction.getId());
        String confidenceText = String.format("%.0f%%", confidence * 100);
        confidenceLabel.setText(confidenceText);

        // 设置分类原因
        String reason = classificationService.getClassificationReason(transaction.getId());
        reasonLabel.setText(reason);

        // 过滤类别列表，只显示与交易类型匹配的类别
        CategoryType categoryType = transaction.getType() == TransactionType.INCOME ?
                CategoryType.INCOME : CategoryType.EXPENSE;

        List<Category> filteredCategories = categories.stream()
                .filter(c -> c.getType() == categoryType)
                .collect(Collectors.toList());

        // 设置类别下拉框
        categoryComboBox.setItems(FXCollections.observableArrayList(filteredCategories));

        // 默认选择当前类别
        if (transaction.getCategory() != null) {
            for (Category category : filteredCategories) {
                if (category.getId().equals(transaction.getCategory().getId())) {
                    categoryComboBox.setValue(category);
                    break;
                }
            }
        }

        // 设置确认状态
        confirmCheckBox.setSelected(transaction.isCategoryConfirmed());
    }

    /**
     * 设置保存回调
     * @param callback 回调函数
     */
    public void setOnSaveCallback(Consumer<Transaction> callback) {
        this.onSaveCallback = callback;
    }

    /**
     * 处理保存按钮点击
     */
    @FXML
    private void handleSave() {
        Category selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            showErrorAlert("错误", "请选择一个类别");
            return;
        }

        try {
            // 处理类别反馈
            Transaction updatedTransaction = classificationService.processCategoryFeedback(
                    transaction.getId(), selectedCategory.getId());

            // 更新确认状态
            updatedTransaction.setCategoryConfirmed(confirmCheckBox.isSelected());
            apiService.updateTransaction(updatedTransaction);

            // 调用回调
            if (onSaveCallback != null) {
                onSaveCallback.accept(updatedTransaction);
            }

            // 关闭对话框
            closeDialog();
        } catch (IOException e) {
            showErrorAlert("保存失败", "无法更新交易类别: " + e.getMessage());
        }
    }

    /**
     * 处理取消按钮点击
     */
    @FXML
    private void handleCancel() {
        closeDialog();
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