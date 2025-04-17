package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 交易表单控制器
 */
public class TransactionFormController implements Initializable {

    @FXML
    private ComboBox<TransactionType> typeComboBox;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField merchantField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    // 编辑的交易
    private Transaction transaction;

    // 类别列表
    private List<Category> categories;

    // 保存回调
    private Consumer<Transaction> onSaveCallback;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 服务
    private ApiService apiService;

    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();

        // 初始化类型下拉框
        typeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        typeComboBox.setValue(TransactionType.EXPENSE);

        // 监听类型变化，更新类别下拉框
        typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && categories != null) {
                updateCategoryComboBox(newValue);
            }
        });

        // 设置日期选择器默认值为今天
        datePicker.setValue(LocalDate.now());

        // 设置金额输入限制 (只允许数字和小数点)
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty() && !newValue.matches("\\d*(\\.\\d{0,2})?")) {
                amountField.setText(oldValue);
            }
        });
    }

    /**
     * 设置要编辑的交易
     * @param transaction 交易对象
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;

        if (transaction != null) {
            // 填充表单字段
            typeComboBox.setValue(transaction.getType());
            amountField.setText(transaction.getAmount().toString());
            datePicker.setValue(transaction.getTransactionDate().toLocalDate());

            if (transaction.getCategory() != null) {
                categoryComboBox.setValue(transaction.getCategory());
            }

            merchantField.setText(transaction.getMerchant());
            descriptionArea.setText(transaction.getDescription());
        }
    }

    /**
     * 设置类别列表
     * @param categories 类别列表
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories;

        // 更新类别下拉框
        if (typeComboBox.getValue() != null) {
            updateCategoryComboBox(typeComboBox.getValue());
        }
    }

    /**
     * 设置保存回调
     * @param callback 回调函数
     */
    public void setOnSaveCallback(Consumer<Transaction> callback) {
        this.onSaveCallback = callback;
    }

    /**
     * 根据交易类型更新类别下拉框
     * @param type 交易类型
     */
    private void updateCategoryComboBox(TransactionType type) {
        if (categories == null) return;

        // 过滤出当前类型的类别
        List<Category> filteredCategories = categories.stream()
                .filter(category -> category.getType() != null &&
                        (category.getType().toString().equals(type.toString()) ||
                                category.getType().toString().equals("GENERAL")))
                .collect(Collectors.toList());

        categoryComboBox.setItems(FXCollections.observableArrayList(filteredCategories));

        // 设置类别显示格式
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

        // 如果有需要，保留当前选择的类别
        if (transaction != null && transaction.getCategory() != null) {
            Category currentCategory = transaction.getCategory();
            for (Category category : filteredCategories) {
                if (category.getId().equals(currentCategory.getId())) {
                    categoryComboBox.setValue(category);
                    break;
                }
            }
        } else if (!filteredCategories.isEmpty()) {
            categoryComboBox.setValue(filteredCategories.get(0));
        }
    }

    /**
     * 处理保存按钮点击
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            // 获取表单数据
            TransactionType type = typeComboBox.getValue();
            BigDecimal amount = new BigDecimal(amountField.getText());
            LocalDate date = datePicker.getValue();
            Category category = categoryComboBox.getValue();
            String merchant = merchantField.getText();
            String description = descriptionArea.getText();

            // 创建交易日期时间
            LocalDateTime transactionDate = LocalDateTime.of(date, LocalTime.now());

            // 获取当前用户
            User currentUser = appContext.getCurrentUser();
            if (currentUser == null) {
                showErrorAlert("错误", "未检测到登录用户。");
                return;
            }

            // 创建或更新交易对象
            if (transaction == null) {
                // 创建新交易
                transaction = Transaction.builder()
                        .type(type)
                        .amount(amount)
                        .transactionDate(transactionDate)
                        .category(category)
                        .merchant(merchant)
                        .description(description)
                        .user(currentUser)
                        .source("手动输入")
                        .build();

                // 调用API创建交易
                Transaction createdTransaction = apiService.createTransaction(transaction);

                // 调用回调
                if (onSaveCallback != null) {
                    onSaveCallback.accept(createdTransaction);
                }
            } else {
                // 更新现有交易
                transaction.setType(type);
                transaction.setAmount(amount);
                transaction.setTransactionDate(transactionDate);
                transaction.setCategory(category);
                transaction.setMerchant(merchant);
                transaction.setDescription(description);

                // 调用API更新交易
                Transaction updatedTransaction = apiService.updateTransaction(transaction);

                // 调用回调
                if (onSaveCallback != null) {
                    onSaveCallback.accept(updatedTransaction);
                }
            }

            // 关闭对话框
            closeDialog();

        } catch (Exception e) {
            showErrorAlert("保存失败", "保存交易记录时出错: " + e.getMessage());
        }
    }

    /**
     * 验证表单
     * @return 表单是否有效
     */
    private boolean validateForm() {
        // 验证金额
        String amountText = amountField.getText();
        if (amountText == null || amountText.isEmpty()) {
            showErrorAlert("验证错误", "请输入金额。");
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showErrorAlert("验证错误", "金额必须大于零。");
                return false;
            }
        } catch (NumberFormatException e) {
            showErrorAlert("验证错误", "金额格式无效。");
            return false;
        }

        // 验证日期
        if (datePicker.getValue() == null) {
            showErrorAlert("验证错误", "请选择日期。");
            return false;
        }

        // 验证类别
        if (categoryComboBox.getValue() == null) {
            showErrorAlert("验证错误", "请选择类别。");
            return false;
        }

        return true;
    }

    /**
     * 处理取消按钮点击
     */
    @FXML
    private void handleCancel(ActionEvent event) {
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