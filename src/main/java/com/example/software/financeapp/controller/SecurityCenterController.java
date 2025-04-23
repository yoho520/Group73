package com.example.software.financeapp.controller;

import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.FraudDetectionService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SecurityCenterController implements Initializable {

    @FXML
    private Label securityStatusLabel;

    @FXML
    private Label suspiciousCountLabel;

    @FXML
    private Label fraudCountLabel;

    @FXML
    private Label verifiedCountLabel;

    @FXML
    private Label securityScoreLabel;

    @FXML
    private VBox suspiciousTransactionsContainer;

    @FXML
    private Label noSuspiciousLabel;

    @FXML
    private Button scanButton;

    private ApiService apiService;
    private FraudDetectionService fraudDetectionService;
    private List<Transaction> allTransactions = new ArrayList<>();
    private Map<Transaction, List<String>> suspiciousTransactions;
    private Long userId = 1L; // 使用模拟用户ID

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = new ApiService("https://api.example.com");
        fraudDetectionService = new FraudDetectionService();

        loadTransactions();
    }

    /**
     * 加载交易数据
     */
    private void loadTransactions() {
        try {
            allTransactions = apiService.getTransactions(
                    userId, // 使用固定的模拟用户ID
                    0,
                    1000 // 获取较大数量交易
            );

            updateSecurityStatistics();
            scanTransactions();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "加载失败", "无法加载交易数据: " + e.getMessage());
        }
    }

    /**
     * 更新安全统计数据
     */
    private void updateSecurityStatistics() {
        int fraudCount = (int) allTransactions.stream()
                .filter(Transaction::isFraudulent)
                .count();

        int verifiedCount = (int) allTransactions.stream()
                .filter(Transaction::isVerified)
                .count();

        suspiciousTransactions = fraudDetectionService.detectSuspiciousTransactions(allTransactions);
        int suspiciousCount = suspiciousTransactions.size();

        // 更新UI
        fraudCountLabel.setText(String.valueOf(fraudCount));
        verifiedCountLabel.setText(String.valueOf(verifiedCount));
        suspiciousCountLabel.setText(String.valueOf(suspiciousCount));

        // 计算安全分数 (示例算法)
        int totalCount = allTransactions.size();
        int safeCount = totalCount - suspiciousCount - fraudCount;
        int securityScore = totalCount > 0
                ? (int) (((double) safeCount / totalCount) * 100)
                : 100;

        // 调整分数
        securityScore = Math.max(60, Math.min(99, securityScore));
        securityScoreLabel.setText(securityScore + "/100");

        // 更新安全状态
        if (fraudCount > 0) {
            securityStatusLabel.setText("需要注意");
            securityStatusLabel.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");
        } else if (suspiciousCount > 0) {
            securityStatusLabel.setText("需要检查");
            securityStatusLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");
        } else {
            securityStatusLabel.setText("正常");
            securityStatusLabel.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 15;");
        }
    }

    /**
     * 扫描交易寻找可疑交易
     */
    @FXML
    private void handleScanTransactions() {
        scanTransactions();
    }

    private void scanTransactions() {
        suspiciousTransactionsContainer.getChildren().clear();

        if (suspiciousTransactions == null || suspiciousTransactions.isEmpty()) {
            suspiciousTransactionsContainer.getChildren().add(noSuspiciousLabel);
            return;
        }

        noSuspiciousLabel.setVisible(false);
        noSuspiciousLabel.setManaged(false);

        // 将可疑交易按风险等级排序（从高到低）
        List<Map.Entry<Transaction, List<String>>> sortedSuspicious = suspiciousTransactions.entrySet().stream()
                .sorted((e1, e2) -> {
                    int risk1 = fraudDetectionService.calculateRiskLevel(e1.getKey(), e1.getValue());
                    int risk2 = fraudDetectionService.calculateRiskLevel(e2.getKey(), e2.getValue());
                    return Integer.compare(risk2, risk1); // 从高到低排序
                })
                .collect(Collectors.toList());

        for (Map.Entry<Transaction, List<String>> entry : sortedSuspicious) {
            Transaction transaction = entry.getKey();
            List<String> reasons = entry.getValue();

            // 跳过已处理的交易
            if (transaction.isFraudulent() || transaction.isVerified()) {
                continue;
            }

            // 计算风险等级
            int riskLevel = fraudDetectionService.calculateRiskLevel(transaction, reasons);
            String riskDescription = fraudDetectionService.getRiskLevelDescription(riskLevel);

            // 添加交易卡片
            VBox transactionCard = createSuspiciousTransactionCard(transaction, reasons, riskLevel, riskDescription);
            suspiciousTransactionsContainer.getChildren().add(transactionCard);
        }

        if (suspiciousTransactionsContainer.getChildren().isEmpty()) {
            noSuspiciousLabel.setVisible(true);
            noSuspiciousLabel.setManaged(true);
            suspiciousTransactionsContainer.getChildren().add(noSuspiciousLabel);
        }
    }

    /**
     * 创建可疑交易卡片
     */
    private VBox createSuspiciousTransactionCard(Transaction transaction,
                                                 List<String> reasons,
                                                 int riskLevel,
                                                 String riskDescription) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f5f5f5; -fx-background-radius: 5;");

        // 交易基本信息
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);

        Label merchantLabel = new Label(transaction.getDescription());
        merchantLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        merchantLabel.setMaxWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(String.format("¥%.2f", transaction.getAmount()));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // 风险等级标签
        Label riskLabel = new Label(riskDescription);
        riskLabel.setPadding(new Insets(2, 8, 2, 8));
        riskLabel.setTextFill(Color.WHITE);

        // 根据风险等级设置颜色
        switch (riskLevel) {
            case 2:
                riskLabel.setStyle("-fx-background-color: #FFA500; -fx-background-radius: 3;");
                break;
            case 3:
                riskLabel.setStyle("-fx-background-color: #FF6600; -fx-background-radius: 3;");
                break;
            case 4:
            case 5:
                riskLabel.setStyle("-fx-background-color: #FF0000; -fx-background-radius: 3;");
                break;
            default:
                riskLabel.setStyle("-fx-background-color: #00AA00; -fx-background-radius: 3;");
        }

        headerBox.getChildren().addAll(merchantLabel, spacer, amountLabel, riskLabel);

        // 交易时间和位置
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr = transaction.getTransactionDate().format(formatter);

        // 由于Transaction类没有getLocation方法，这里使用merchant或source信息代替
        String locationStr = transaction.getMerchant() != null && !transaction.getMerchant().isEmpty()
                ? transaction.getMerchant() : "未知位置";

        Label detailsLabel = new Label(dateStr + " | " + locationStr);
        detailsLabel.setTextFill(Color.GRAY);

        // 可疑原因
        VBox reasonsBox = new VBox(5);
        Label reasonsTitle = new Label("预警原因：");
        reasonsTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
        reasonsBox.getChildren().add(reasonsTitle);

        for (String reason : reasons) {
            HBox reasonRow = new HBox(5);
            Label bullet = new Label("•");
            bullet.setTextFill(Color.RED);

            Label reasonLabel = new Label(reason);
            reasonLabel.setWrapText(true);

            reasonRow.getChildren().addAll(bullet, reasonLabel);
            reasonsBox.getChildren().add(reasonRow);
        }

        // 操作按钮
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button reportButton = new Button("报告欺诈");
        reportButton.getStyleClass().add("danger");
        reportButton.setOnAction(e -> reportFraudTransaction(transaction));

        Button ignoreButton = new Button("确认安全");
        ignoreButton.getStyleClass().add("success");
        ignoreButton.setOnAction(e -> markTransactionSafe(transaction));

        buttonsBox.getChildren().addAll(reportButton, ignoreButton);

        // 组装卡片
        card.getChildren().addAll(headerBox, detailsLabel, reasonsBox, buttonsBox);

        return card;
    }

    /**
     * 报告欺诈交易
     */
    private void reportFraudTransaction(Transaction transaction) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认报告欺诈");
        alert.setHeaderText("确认将此交易标记为欺诈交易？");
        alert.setContentText("这将帮助系统提高欺诈检测能力，并可能触发相关安全措施。");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                transaction.setFraudulent(true);
                apiService.updateTransaction(transaction);

                showAlert(Alert.AlertType.INFORMATION, "操作成功", "交易已标记为欺诈交易");

                // 更新UI
                updateSecurityStatistics();
                scanTransactions();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "操作失败", "无法更新交易状态: " + e.getMessage());
            }
        }
    }

    /**
     * 标记交易为安全
     */
    private void markTransactionSafe(Transaction transaction) {
        try {
            transaction.setVerified(true);
            apiService.updateTransaction(transaction);

            showAlert(Alert.AlertType.INFORMATION, "操作成功", "交易已标记为安全");

            // 更新UI
            updateSecurityStatistics();
            scanTransactions();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "操作失败", "无法更新交易状态: " + e.getMessage());
        }
    }

    /**
     * 刷新数据
     */
    @FXML
    private void handleRefresh() {
        loadTransactions();
    }

    /**
     * 显示提示对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}