package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.ai.ClassificationService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import com.example.software.financeapp.controller.TransactionFormController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
/**
 * 交易管理控制器
 * 负责管理交易列表、添加、编辑和导入交易
 */
public class TransactionController implements Initializable {

    @FXML
    private TableView<Transaction> transactionTable;

    @FXML
    private TableColumn<Transaction, LocalDateTime> dateColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;

    @FXML
    private TableColumn<Transaction, String> categoryColumn;

    @FXML
    private TableColumn<Transaction, String> merchantColumn;

    @FXML
    private TableColumn<Transaction, String> descriptionColumn;

    @FXML
    private TableColumn<Transaction, Boolean> categoryConfirmedColumn;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private ComboBox<Category> categoryFilterComboBox;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button importButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button classifyButton;

    @FXML
    private Label totalIncomeLabel;

    @FXML
    private Label totalExpenseLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Pagination pagination;

    // 数据相关属性
    private ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredTransactions;
    private List<Category> categories;

    // 分页相关属性
    private static final int PAGE_SIZE = 25;
    private int totalPages = 1;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // 服务
    private ApiService apiService;
    private ClassificationService classificationService;
    @FXML
    private VBox filterPanel;



    /**
     * 处理应用筛选按钮点击
     */


    @FXML
    private void toggleFilterPanel() {
        filterPanel.setVisible(!filterPanel.isVisible());
    }

    @FXML
    private void handleApplyFilters() {
        applyFilters(); // 调用已有的过滤方法
    }

    @FXML


    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();
        this.classificationService = appContext.getClassificationService();

        // 初始化表格列
        initializeTableColumns();

        // 初始化过滤器
        initializeFilters();

        // 初始化按钮状态
        initializeButtons();

        // 加载数据
        loadData();
    }

    /**
     * 初始化表格列
     */
    private void initializeTableColumns() {
        // 日期列
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        dateColumn.setCellFactory(column -> new TableCell<Transaction, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatter.format(item));
                }
            }
        });

        // 类型列
        typeColumn.setCellValueFactory(cellData -> {
            TransactionType type = cellData.getValue().getType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "");
        });

        // 金额列
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setCellFactory(column -> new TableCell<Transaction, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // 获取当前行的事务对象
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        Transaction transaction = getTableView().getItems().get(index);
                        setText(String.format("¥%.2f", item));

                        if (transaction.getType() == TransactionType.EXPENSE) {
                            setStyle("-fx-text-fill: #e74c3c;"); // 红色
                        } else if (transaction.getType() == TransactionType.INCOME) {
                            setStyle("-fx-text-fill: #2ecc71;"); // 绿色
                        }
                    } else {
                        setText(String.format("¥%.2f", item));
                    }
                }
            }
        });

// 在这里添加categoryConfirmedColumn的初始化代码
        categoryConfirmedColumn.setText("AI");
        categoryConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("categoryConfirmed"));
        categoryConfirmedColumn.setCellFactory(column -> new TableCell<Transaction, Boolean>() {
            // 加载大尺寸图标并缩放到合适大小
            private final ImageView confirmedIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/confirmed.png"), 16, 16, true, true));
            private final ImageView aiIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/ai.png"), 16, 16, true, true));

            @Override
            protected void updateItem(Boolean confirmed, boolean empty) {
                super.updateItem(confirmed, empty);
                if (empty) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    if (confirmed != null && confirmed) {
                        setGraphic(confirmedIcon);
                        setTooltip(new Tooltip("用户确认的类别"));
                    } else {
                        setGraphic(aiIcon);

                        // 获取置信度
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        double confidence = classificationService.getClassificationConfidence(transaction.getId());
                        String reason = classificationService.getClassificationReason(transaction.getId());

                        setTooltip(new Tooltip(String.format(
                                "AI分类 (置信度: %.0f%%)\n%s", confidence * 100, reason)));
                    }
                }
            }
        });
        categoryConfirmedColumn.setPrefWidth(40);
        // 类别列
        categoryColumn.setCellValueFactory(cellData -> {
            Category category = cellData.getValue().getCategory();
            return new SimpleStringProperty(category != null ? category.getName() : "未分类");
        });

        // 类别确认状态列
        categoryConfirmedColumn.setText("AI");
        categoryConfirmedColumn.setCellValueFactory(new PropertyValueFactory<>("categoryConfirmed"));
        categoryConfirmedColumn.setCellFactory(column -> new TableCell<Transaction, Boolean>() {
            private final ImageView confirmedIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/confirmed.png"), 16, 16, true, true));
            private final ImageView aiIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/ai.png"), 16, 16, true, true));

            @Override
            protected void updateItem(Boolean confirmed, boolean empty) {
                super.updateItem(confirmed, empty);
                if (empty) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    if (confirmed != null && confirmed) {
                        setGraphic(confirmedIcon);
                        setTooltip(new Tooltip("用户确认的类别"));
                    } else {
                        setGraphic(aiIcon);

                        // 获取置信度
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        double confidence = classificationService.getClassificationConfidence(transaction.getId());
                        String reason = classificationService.getClassificationReason(transaction.getId());

                        setTooltip(new Tooltip(String.format(
                                "AI分类 (置信度: %.0f%%)\n%s", confidence * 100, reason)));
                    }
                }
            }
        });
        categoryConfirmedColumn.setPrefWidth(40);

        // 商家列
        merchantColumn.setCellValueFactory(new PropertyValueFactory<>("merchant"));

        // 描述列
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 设置表格行双击事件
        transactionTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Transaction transaction = row.getItem();
                    if (event.getButton() == MouseButton.PRIMARY) {
                        handleEditTransaction(new ActionEvent());
                    }
                }
            });

            // 添加右键菜单
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("编辑交易");
            editItem.setOnAction(event -> handleEditTransaction(new ActionEvent()));

            MenuItem categoryItem = new MenuItem("修改分类");
            categoryItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    handleEditCategory(row.getItem());
                }
            });

            MenuItem deleteItem = new MenuItem("删除交易");
            deleteItem.setOnAction(event -> handleDeleteTransaction(new ActionEvent()));

            contextMenu.getItems().addAll(editItem, categoryItem, deleteItem);
            row.setContextMenu(contextMenu);

            return row;
        });

        // 监听表格选择变化，更新按钮状态
        transactionTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateButtonsState(newValue != null));
    }

    /**
     * 初始化过滤器
     */
    private void initializeFilters() {
        // 初始化日期选择器
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // 设置日期选择器变更监听
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        endDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // 初始化类型过滤下拉框
        typeFilterComboBox.getItems().addAll(
                "全部",
                TransactionType.EXPENSE.getDisplayName(),
                TransactionType.INCOME.getDisplayName(),
                TransactionType.TRANSFER.getDisplayName()
        );
        typeFilterComboBox.setValue("全部");
        typeFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // 搜索框变更监听
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * 初始化按钮状态
     */
    private void initializeButtons() {
        // 禁用需要选择交易才能操作的按钮
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    /**
     * 更新按钮状态
     * @param hasSelection 是否有选中的交易
     */
    private void updateButtonsState(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * 加载数据
     */
    private void loadData() {
        try {
            // 获取当前用户
            User currentUser = appContext.getCurrentUser();
            if (currentUser == null) return;

            // 加载交易数据
            List<Transaction> transactions = apiService.getTransactions(currentUser.getId(), 0, 100);

            // 加载类别数据
            categories = apiService.getCategories(currentUser.getId());

            // 设置类别过滤下拉框
            categoryFilterComboBox.getItems().clear();
            categoryFilterComboBox.getItems().add(null); // 添加"全部"选项
            categoryFilterComboBox.getItems().addAll(categories);
            categoryFilterComboBox.setValue(null);
            categoryFilterComboBox.setCellFactory(param -> new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("全部");
                    } else {
                        setText(item.getName());
                    }
                }
            });
            categoryFilterComboBox.setButtonCell(new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("全部");
                    } else {
                        setText(item.getName());
                    }
                }
            });
            categoryFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

            // 更新表格数据
            allTransactions.clear();
            allTransactions.addAll(transactions);
            filteredTransactions = new FilteredList<>(allTransactions);

            // 应用过滤器
            applyFilters();

            // 更新统计信息
            updateStatistics();

        } catch (IOException e) {
            showErrorAlert("加载数据失败", e.getMessage());
        }
    }

    /**
     * 应用过滤器
     */
    private void applyFilters() {
        Predicate<Transaction> datePredicate = transaction -> {
            LocalDate transactionDate = transaction.getTransactionDate().toLocalDate();
            return (startDatePicker.getValue() == null || !transactionDate.isBefore(startDatePicker.getValue())) &&
                    (endDatePicker.getValue() == null || !transactionDate.isAfter(endDatePicker.getValue()));
        };

        Predicate<Transaction> typePredicate = transaction -> {
            String selectedType = typeFilterComboBox.getValue();
            return "全部".equals(selectedType) ||
                    (transaction.getType() != null &&
                            transaction.getType().getDisplayName().equals(selectedType));
        };

        Predicate<Transaction> categoryPredicate = transaction -> {
            Category selectedCategory = categoryFilterComboBox.getValue();
            return selectedCategory == null ||
                    (transaction.getCategory() != null &&
                            transaction.getCategory().getId().equals(selectedCategory.getId()));
        };

        Predicate<Transaction> searchPredicate = transaction -> {
            String searchText = searchField.getText().toLowerCase();
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            // 搜索商家、描述、金额等字段
            return (transaction.getMerchant() != null &&
                    transaction.getMerchant().toLowerCase().contains(searchText)) ||
                    (transaction.getDescription() != null &&
                            transaction.getDescription().toLowerCase().contains(searchText)) ||
                    transaction.getAmount().toString().contains(searchText);
        };

        // 组合所有过滤条件
        filteredTransactions.setPredicate(
                datePredicate.and(typePredicate).and(categoryPredicate).and(searchPredicate));

        // 更新表格数据
        transactionTable.setItems(filteredTransactions);

        // 更新统计信息
        updateStatistics();
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction transaction : filteredTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(transaction.getAmount());
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        totalIncomeLabel.setText(String.format("¥%.2f", totalIncome));
        totalExpenseLabel.setText(String.format("¥%.2f", totalExpense));
        balanceLabel.setText(String.format("¥%.2f", balance));

        // 设置差额颜色
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balanceLabel.setStyle("-fx-text-fill: #e74c3c;"); // 红色
        } else {
            balanceLabel.setStyle("-fx-text-fill: #2ecc71;"); // 绿色
        }
    }

    /**
     * 处理添加交易
     */
    @FXML
    private void handleAddTransaction(ActionEvent event) {
        try {
            // 加载交易表单对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/transaction_form.fxml"));
            Parent root = loader.load();

            // 获取控制器
            TransactionFormController controller = loader.getController();

            // 设置类别列表
            controller.setCategories(categories);

            // 设置保存回调
            controller.setOnSaveCallback(savedTransaction -> {
                // 刷新数据
                loadData();
            });

            // 创建对话框
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(appContext.getPrimaryStage());
            dialog.setTitle("添加交易");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showErrorAlert("加载交易表单失败", e.getMessage());
        }
    }

    /**
     * 处理编辑交易
     */
    @FXML
    private void handleEditTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            try {
                // 加载交易表单对话框
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/transaction_form.fxml"));
                Parent root = loader.load();

                // 获取控制器
                TransactionFormController controller = loader.getController();

                // 设置类别列表
                controller.setCategories(categories);

                // 设置交易
                controller.setTransaction(selectedTransaction);

                // 设置保存回调
                controller.setOnSaveCallback(savedTransaction -> {
                    // 刷新数据
                    loadData();
                });

                // 创建对话框
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(appContext.getPrimaryStage());
                dialog.setTitle("编辑交易");
                dialog.setScene(new Scene(root));
                dialog.showAndWait();
            } catch (IOException e) {
                showErrorAlert("加载交易表单失败", e.getMessage());
            }
        }
    }

    /**
     * 处理编辑类别
     */
    private void handleEditCategory(Transaction transaction) {
        try {
            // 加载类别反馈对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/category_feedback.fxml"));
            Parent root = loader.load();

            // 获取控制器
            CategoryFeedbackController controller = loader.getController();

            // 设置交易和类别列表
            controller.setTransaction(transaction, categories);

            // 设置保存回调
            controller.setOnSaveCallback(updatedTransaction -> {
                // 刷新数据
                loadData();
            });

            // 创建对话框
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(appContext.getPrimaryStage());
            dialog.setTitle("修改交易分类");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            showErrorAlert("加载类别反馈对话框失败", e.getMessage());
        }
    }

    /**
     * 处理删除交易
     */
    @FXML
    private void handleDeleteTransaction(ActionEvent event) {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            showConfirmAlert("确认删除", "是否确定要删除所选交易记录？", () -> {
                try {
                    apiService.deleteTransaction(selectedTransaction.getId());
                    allTransactions.remove(selectedTransaction);
                    applyFilters();
                    showInfoAlert("删除成功", "交易记录已成功删除");
                } catch (IOException e) {
                    showErrorAlert("删除失败", e.getMessage());
                }
            });
        }
    }

    /**
     * 批量自动分类
     */
    @FXML
    private void handleBatchClassify(ActionEvent event) {
        // 获取未分类或未确认的交易
        List<Transaction> unclassifiedTransactions = allTransactions.stream()
                .filter(t -> t.getCategory() == null || !t.isCategoryConfirmed())
                .collect(java.util.stream.Collectors.toList());

        if (unclassifiedTransactions.isEmpty()) {
            showInfoAlert("自动分类", "没有需要分类的交易记录");
            return;
        }

        showConfirmAlert("自动分类", "系统将对" + unclassifiedTransactions.size() + "条未分类或未确认的交易记录进行AI分类，是否继续？", () -> {
            try {
                // 批量分类
                classificationService.classifyTransactions(unclassifiedTransactions);

                // 更新交易记录
                for (Transaction transaction : unclassifiedTransactions) {
                    apiService.updateTransaction(transaction);
                }

                // 刷新数据
                loadData();

                showInfoAlert("自动分类完成", "成功分类" + unclassifiedTransactions.size() + "条交易记录");
            } catch (Exception e) {
                showErrorAlert("自动分类失败", e.getMessage());
            }
        });
    }

    /**
     * 处理导入交易
     */
    @FXML
    private void handleImportTransactions(ActionEvent event) {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择CSV文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv"));

        // 显示文件选择对话框
        File selectedFile = fileChooser.showOpenDialog(appContext.getPrimaryStage());

        if (selectedFile != null) {
            try {
                // 加载导入选项对话框
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/import_options.fxml"));
                Parent root = loader.load();

                // 获取控制器
                ImportOptionsController controller = loader.getController();

                // 设置文件
                controller.setFile(selectedFile);

                // 设置导入完成回调
                controller.setOnImportFinishedCallback(importedTransactions -> {
                    // 自动分类导入的交易
                    try {
                        classificationService.classifyTransactions(importedTransactions);
                        // 更新交易记录
                        for (Transaction transaction : importedTransactions) {
                            apiService.updateTransaction(transaction);
                        }
                    } catch (Exception e) {
                        System.err.println("自动分类导入交易失败: " + e.getMessage());
                    }

                    // 刷新数据
                    loadData();

                    // 显示统计摘要
                    int incomeCount = 0;
                    int expenseCount = 0;

                    for (Transaction t : importedTransactions) {
                        if (t.getType() == TransactionType.INCOME) {
                            incomeCount++;
                        } else {
                            expenseCount++;
                        }
                    }

                    showInfoAlert("导入摘要", String.format(
                            "成功导入%d条交易记录:\n- %d条收入\n- %d条支出\n\n已自动进行智能分类",
                            importedTransactions.size(), incomeCount, expenseCount));
                });

                // 创建对话框
                Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(appContext.getPrimaryStage());
                dialog.setTitle("导入交易记录");
                dialog.setScene(new Scene(root));
                dialog.showAndWait();

            } catch (IOException e) {
                showErrorAlert("加载导入选项失败", e.getMessage());
            }
        }
    }

    /**
     * 处理导出交易
     */
    @FXML
    private void handleExportTransactions(ActionEvent event) {
        showInfoAlert("功能提示", "导出交易功能将在后续实现");
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
    /**
     * 处理修改类别菜单项点击
     */
    @FXML
    private void handleEditCategory(ActionEvent event) {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            openCategoryFeedbackDialog(selectedTransaction);
        }
    }

    /**
     * 打开类别反馈对话框
     * @param transaction 要修改分类的交易
     */
    private void openCategoryFeedbackDialog(Transaction transaction) {
        try {
            // 加载类别反馈对话框
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/category_feedback.fxml"));
            Parent root = loader.load();

            // 获取控制器
            CategoryFeedbackController controller = loader.getController();

            // 设置交易和类别列表
            controller.setTransaction(transaction, categories);

            // 设置保存回调
            controller.setOnSaveCallback(updatedTransaction -> {
                // 刷新数据
                loadData();
            });

            // 创建对话框
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(appContext.getPrimaryStage());
            dialog.setTitle("修改交易分类");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            showErrorAlert("加载类别反馈对话框失败", e.getMessage());
        }
    }
}