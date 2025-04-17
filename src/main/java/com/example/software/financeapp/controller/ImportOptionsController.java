package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.util.CSVParser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * CSV导入选项控制器
 */
public class ImportOptionsController implements Initializable {

    @FXML
    private ComboBox<CSVParser.CSVSourceType> sourceTypeComboBox;

    @FXML
    private TableView<Transaction> previewTable;

    @FXML
    private Label fileNameLabel;

    @FXML
    private Label recordCountLabel;

    @FXML
    private Button previewButton;

    @FXML
    private Button importButton;

    @FXML
    private Button cancelButton;

    // 文件
    private File file;

    // 预览的交易记录
    private List<Transaction> previewTransactions;

    // 导入完成回调
    private Consumer<List<Transaction>> onImportFinishedCallback;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // API服务
    private ApiService apiService;

    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.apiService = appContext.getApiService();

        // 初始化源类型下拉框
        sourceTypeComboBox.setItems(FXCollections.observableArrayList(CSVParser.CSVSourceType.values()));
        sourceTypeComboBox.setCellFactory(param -> new ListCell<CSVParser.CSVSourceType>() {
            @Override
            protected void updateItem(CSVParser.CSVSourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    switch (item) {
                        case ALIPAY:
                            setText("支付宝");
                            break;
                        case WECHAT:
                            setText("微信支付");
                            break;
                        case BANK:
                            setText("银行对账单");
                            break;
                        case CUSTOM:
                            setText("自定义格式");
                            break;
                        default:
                            setText(item.toString());
                    }
                }
            }
        });

        sourceTypeComboBox.setButtonCell(new ListCell<CSVParser.CSVSourceType>() {
            @Override
            protected void updateItem(CSVParser.CSVSourceType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("请选择源类型");
                } else {
                    switch (item) {
                        case ALIPAY:
                            setText("支付宝");
                            break;
                        case WECHAT:
                            setText("微信支付");
                            break;
                        case BANK:
                            setText("银行对账单");
                            break;
                        case CUSTOM:
                            setText("自定义格式");
                            break;
                        default:
                            setText(item.toString());
                    }
                }
            }
        });

        // 设置默认源类型
        sourceTypeComboBox.setValue(CSVParser.CSVSourceType.ALIPAY);

        // 初始化按钮状态
        importButton.setDisable(true);

        // 初始化表格
        initializePreviewTable();
    }

    /**
     * 初始化预览表格
     */
    private void initializePreviewTable() {
        // 日期列
        TableColumn<Transaction, String> dateColumn = new TableColumn<>("日期");
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return javafx.beans.binding.Bindings.createStringBinding(
                    () -> cellData.getValue().getTransactionDate().format(formatter));
        });
        dateColumn.setPrefWidth(140);

        // 类型列
        TableColumn<Transaction, String> typeColumn = new TableColumn<>("类型");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeColumn.setPrefWidth(80);

        // 金额列
        TableColumn<Transaction, String> amountColumn = new TableColumn<>("金额");
        amountColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> String.format("%.2f", cellData.getValue().getAmount())));
        amountColumn.setPrefWidth(100);

        // 商家列
        TableColumn<Transaction, String> merchantColumn = new TableColumn<>("商家");
        merchantColumn.setCellValueFactory(new PropertyValueFactory<>("merchant"));
        merchantColumn.setPrefWidth(150);

        // 描述列
        TableColumn<Transaction, String> descriptionColumn = new TableColumn<>("描述");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setPrefWidth(200);

        // 类别列
        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("类别");
        categoryColumn.setCellValueFactory(cellData -> {
            return javafx.beans.binding.Bindings.createStringBinding(() -> {
                if (cellData.getValue().getCategory() != null) {
                    return cellData.getValue().getCategory().getName();
                }
                return "未分类";
            });
        });
        categoryColumn.setPrefWidth(120);

// 添加列到表格
        previewTable.getColumns().addAll(dateColumn, typeColumn, amountColumn,
                merchantColumn, descriptionColumn, categoryColumn);
    }

    /**
     * 设置文件
     *
     * @param file CSV文件
     */
    public void setFile(File file) {
        this.file = file;
        fileNameLabel.setText(file.getName());
    }

    /**
     * 设置导入完成回调
     *
     * @param callback 回调函数
     */
    public void setOnImportFinishedCallback(Consumer<List<Transaction>> callback) {
        this.onImportFinishedCallback = callback;
    }

    /**
     * 处理预览按钮点击
     */
    @FXML
    private void handlePreview(ActionEvent event) {
        if (file == null) {
            showErrorAlert("错误", "请先选择CSV文件。");
            return;
        }

        if (sourceTypeComboBox.getValue() == null) {
            showErrorAlert("错误", "请选择数据源类型。");
            return;
        }

        try {
            // 清空表格
            previewTable.getItems().clear();

            // 创建解析器
            CSVParser parser = new CSVParser(
                    sourceTypeComboBox.getValue(),
                    appContext.getCurrentUser());

            // 解析文件
            previewTransactions = parser.parseFile(file);

            // 更新记录数量
            recordCountLabel.setText(String.valueOf(previewTransactions.size()));

            // 显示预览数据（最多显示50条）
            ObservableList<Transaction> previewData = FXCollections.observableArrayList(
                    previewTransactions.subList(0, Math.min(50, previewTransactions.size())));
            previewTable.setItems(previewData);

            // 启用导入按钮
            importButton.setDisable(previewTransactions.isEmpty());

        } catch (Exception e) {
            showErrorAlert("解析错误", "解析CSV文件时出错: " + e.getMessage());
        }
    }

    /**
     * 处理导入按钮点击
     */
    @FXML
    private void handleImport(ActionEvent event) {
        if (previewTransactions == null || previewTransactions.isEmpty()) {
            showErrorAlert("错误", "没有可导入的数据。");
            return;
        }

        try {
            // 显示进度指示器
            showLoadingIndicator(true);

            // 批量保存交易记录
            List<Transaction> importedTransactions = apiService.createTransactions(previewTransactions);

            // 隐藏进度指示器
            showLoadingIndicator(false);

            // 显示成功消息
            showSuccessAlert("导入成功", "成功导入 " + importedTransactions.size() + " 条交易记录。");

            // 调用回调
            if (onImportFinishedCallback != null) {
                onImportFinishedCallback.accept(importedTransactions);
            }

            // 关闭对话框
            closeDialog();

        } catch (Exception e) {
            // 隐藏进度指示器
            showLoadingIndicator(false);

            showErrorAlert("导入错误", "导入交易记录时出错: " + e.getMessage());
        }
    }

    /**
     * 处理取消按钮点击
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeDialog();
    }

    /**
     * 显示/隐藏加载指示器
     */
    private void showLoadingIndicator(boolean show) {
        // 在实际项目中实现加载指示器
        previewButton.setDisable(show);
        importButton.setDisable(show);
        cancelButton.setDisable(show);
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

    /**
     * 显示成功提示对话框
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}