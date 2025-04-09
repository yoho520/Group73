package com.stockanalyzer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;

public class StockAnalyzerApp extends Application {

    private ComboBox<Pair<String, String>> modelSelector;
    private TextField stockNameField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private TextArea chatInputArea;
    private TextArea chatHistoryArea;
    private WebView reportWebView;
    private Tab analysisTab;
    private Tab chatTab;
    private Button analyzeButton;
    private Button sendButton;
    private Label statusLabel;

    // API端点
    private static final String BASE_URL = "http://localhost:8000";
    private static final String ANALYZE_ENDPOINT = BASE_URL + "/api/analyze";
    private static final String CHAT_ENDPOINT = BASE_URL + "/api/chat";
    private static final String MODELS_ENDPOINT = BASE_URL + "/api/models";

    // 聊天历史记录
    private List<JSONObject> chatHistory = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("FinChat-智能股票分析助手");

        // 创建主布局
        BorderPane mainLayout = new BorderPane();

        // 创建标签页面板
        TabPane tabPane = new TabPane();
        analysisTab = new Tab("分析报告");
        analysisTab.setClosable(false);
        chatTab = new Tab("聊天");
        chatTab.setClosable(false);

        // 设置分析报告页
        VBox analysisLayout = new VBox(10);
        analysisLayout.setPadding(new Insets(10));
        analysisLayout.setStyle("-fx-background-color: #333333;");

        reportWebView = new WebView();
        reportWebView.getEngine().setUserStyleSheetLocation(getClass().getResource("/styles/report.css").toString());

        // 设置分析报告页内容
        analysisLayout.getChildren().add(reportWebView);
        analysisTab.setContent(analysisLayout);

        // 设置聊天页
        BorderPane chatLayout = new BorderPane();
        chatLayout.setPadding(new Insets(10));
        chatLayout.setStyle("-fx-background-color: #333333;");

        // 创建聊天历史区域
        chatHistoryArea = new TextArea();
        chatHistoryArea.setEditable(false);
        chatHistoryArea.setPrefHeight(400);
        chatHistoryArea.setStyle("-fx-control-inner-background: #444444; -fx-text-fill: white;");

        // 创建聊天输入区域
        chatInputArea = new TextArea();
        chatInputArea.setPrefRowCount(3);
        chatInputArea.setPromptText("输入消息...");
        chatInputArea.setStyle("-fx-control-inner-background: #444444; -fx-text-fill: white;");

        // 发送按钮
        sendButton = new Button("发送");
        sendButton.setOnAction(event -> sendChatMessage());

        // 组装聊天输入区域
        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(chatInputArea, sendButton);
        HBox.setHgrow(chatInputArea, Priority.ALWAYS);

        // 组装聊天页面
        chatLayout.setCenter(chatHistoryArea);
        chatLayout.setBottom(inputBox);
        chatTab.setContent(chatLayout);

        // 添加标签页到面板
        tabPane.getTabs().addAll(chatTab, analysisTab);

        // 创建侧边面板
        VBox sidePanel = new VBox(10);
        sidePanel.setPadding(new Insets(10));
        sidePanel.setPrefWidth(300);
        sidePanel.setStyle("-fx-background-color: #333333;");

        // 添加LLM模型选择
        Label modelLabel = new Label("选择LLM模型");
        modelLabel.setStyle("-fx-text-fill: #cccccc;");
        modelSelector = new ComboBox<>();
        modelSelector.setMaxWidth(Double.MAX_VALUE);
        modelSelector.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");

        // 添加股票名称输入
        Label stockLabel = new Label("股票名称");
        stockLabel.setStyle("-fx-text-fill: #cccccc;");
        stockNameField = new TextField();
        stockNameField.setPromptText("例如：中兴通讯");
        stockNameField.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");

        // 添加日期选择器
        Label dateRangeLabel = new Label("日期范围");
        dateRangeLabel.setStyle("-fx-text-fill: #cccccc;");

        // 开始日期
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(1));
        startDatePicker.setPrefWidth(Double.MAX_VALUE);
        startDatePicker.setStyle("-fx-background-color: #444444;");

        // 结束日期
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPrefWidth(Double.MAX_VALUE);
        endDatePicker.setStyle("-fx-background-color: #444444;");

        // 分析按钮
        analyzeButton = new Button("开始分析");
        analyzeButton.setMaxWidth(Double.MAX_VALUE);
        analyzeButton.setStyle("-fx-background-color: #4698e2; -fx-text-fill: white;");
        analyzeButton.setOnAction(event -> analyzeStock());

        // 添加状态标签
        statusLabel = new Label("准备就绪");
        statusLabel.setStyle("-fx-text-fill: #cccccc;");

        // 组装侧边面板
        sidePanel.getChildren().addAll(
                modelLabel, modelSelector,
                stockLabel, stockNameField,
                dateRangeLabel,
                new Label("开始日期"), startDatePicker,
                new Label("结束日期"), endDatePicker,
                analyzeButton,
                statusLabel
        );

        // 组装主布局
        mainLayout.setLeft(sidePanel);
        mainLayout.setCenter(tabPane);

        // 创建场景并设置
        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toString());
        primaryStage.setScene(scene);

        // 显示窗口
        primaryStage.show();

        // 初始化加载可用模型
        loadAvailableModels();
    }

    /**
     * 加载可用的模型列表
     */
    private void loadAvailableModels() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(MODELS_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray modelsArray = jsonResponse.getJSONArray("models");

                    Platform.runLater(() -> {
                        for (int i = 0; i < modelsArray.length(); i++) {
                            JSONObject model = modelsArray.getJSONObject(i);
                            String id = model.getString("id");
                            String name = model.getString("name");
                            modelSelector.getItems().add(new Pair<>(id, name));
                        }

                        if (!modelSelector.getItems().isEmpty()) {
                            modelSelector.setValue(modelSelector.getItems().get(0));
                        }
                    });
                } else {
                    Platform.runLater(() -> showError("无法加载模型列表: " + responseCode));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError("加载模型列表时出错: " + e.getMessage()));
            }
        });
    }

    /**
     * 执行股票分析
     */
    private void analyzeStock() {
        // 获取输入
        String stockName = stockNameField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        Pair<String, String> selectedModel = modelSelector.getValue();

        // 验证输入
        if (stockName.isEmpty()) {
            showError("请输入股票名称");
            return;
        }

        if (startDate == null || endDate == null) {
            showError("请选择日期范围");
            return;
        }

        if (selectedModel == null) {
            showError("请选择模型");
            return;
        }

        // 格式化日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDateStr = startDate.format(formatter);
        String endDateStr = endDate.format(formatter);

        // 创建请求JSON
        JSONObject requestBody = new JSONObject();
        requestBody.put("stock_name", stockName);
        requestBody.put("start_date", startDateStr);
        requestBody.put("end_date", endDateStr);
        requestBody.put("chat_model", selectedModel.getKey());

        // 禁用按钮并更新状态
        analyzeButton.setDisable(true);
        statusLabel.setText("分析中...");

        // 执行API请求
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(ANALYZE_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String report = jsonResponse.getString("report");

                    Platform.runLater(() -> {
                        // 使用HTML格式化报告
                        String htmlReport = formatReportAsHtml(report, stockName);
                        reportWebView.getEngine().loadContent(htmlReport);

                        // 切换到分析报告标签
                        TabPane tabPane = analysisTab.getTabPane();
                        tabPane.getSelectionModel().select(analysisTab);

                        // 恢复按钮状态
                        analyzeButton.setDisable(false);
                        statusLabel.setText("分析完成");
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    reader.close();

                    Platform.runLater(() -> {
                        showError("分析失败: " + errorResponse.toString());
                        analyzeButton.setDisable(false);
                        statusLabel.setText("分析失败");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("分析出错: " + e.getMessage());
                    analyzeButton.setDisable(false);
                    statusLabel.setText("分析出错");
                });
            }
        });
    }

    /**
     * 发送聊天消息
     */
    private void sendChatMessage() {
        // 获取输入
        String message = chatInputArea.getText().trim();
        String stockName = stockNameField.getText().trim();
        Pair<String, String> selectedModel = modelSelector.getValue();

        // 验证输入
        if (message.isEmpty()) {
            showError("请输入消息");
            return;
        }

        if (stockName.isEmpty()) {
            showError("请输入股票名称");
            return;
        }

        if (selectedModel == null) {
            showError("请选择模型");
            return;
        }

        // 添加用户消息到聊天历史
        chatHistoryArea.appendText("您: " + message + "\n\n");

        // 清空输入区域
        chatInputArea.clear();

        // 禁用发送按钮
        sendButton.setDisable(true);
        statusLabel.setText("等待回复...");

        // 创建请求JSON
        JSONObject requestBody = new JSONObject();
        requestBody.put("message", message);
        requestBody.put("stock_name", stockName);
        requestBody.put("chat_model", selectedModel.getKey());

        // 添加聊天历史
        JSONArray historyArray = new JSONArray();
        for (JSONObject historyItem : chatHistory) {
            historyArray.put(historyItem);
        }
        requestBody.put("chat_history", historyArray);

        // 执行API请求 - 使用EventSource处理流式响应
        CompletableFuture.runAsync(() -> {
            try {
                // 由于需要处理SSE流式响应，这里使用一个简化的示例
                // 实际实现应该使用支持SSE的Java库，如Jersey或OkHttp

                URL url = new URL(CHAT_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // 模拟流式响应处理
                    // 实际实现应该使用专门的SSE客户端库
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;

                    // 添加AI响应前缀
                    Platform.runLater(() -> chatHistoryArea.appendText("AI: "));

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            JSONObject jsonData = new JSONObject(data);

                            if (jsonData.has("content")) {
                                String content = jsonData.getString("content");
                                responseBuilder.append(content);

                                // 更新UI显示部分响应
                                final String finalContent = content;
                                Platform.runLater(() -> chatHistoryArea.appendText(finalContent));
                            } else if (jsonData.getString("type").equals("done")) {
                                break;
                            } else if (jsonData.has("error")) {
                                String error = jsonData.getString("error");
                                Platform.runLater(() -> showError("聊天错误: " + error));
                                break;
                            }
                        }
                    }
                    reader.close();

                    // 添加新行以准备下一条消息
                    Platform.runLater(() -> chatHistoryArea.appendText("\n\n"));

                    // 添加到聊天历史
                    JSONObject historyItem = new JSONObject();
                    historyItem.put("role", "user");
                    historyItem.put("content", message);
                    chatHistory.add(historyItem);

                    historyItem = new JSONObject();
                    historyItem.put("role", "assistant");
                    historyItem.put("content", responseBuilder.toString());
                    chatHistory.add(historyItem);

                    Platform.runLater(() -> {
                        sendButton.setDisable(false);
                        statusLabel.setText("回复完成");
                    });
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    reader.close();

                    Platform.runLater(() -> {
                        showError("聊天失败: " + errorResponse.toString());
                        sendButton.setDisable(false);
                        statusLabel.setText("聊天失败");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("聊天出错: " + e.getMessage());
                    sendButton.setDisable(false);
                    statusLabel.setText("聊天出错");
                });
            }
        });
    }

    /**
     * 将文本报告格式化为HTML
     */
    private String formatReportAsHtml(String report, String stockName) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <style>\n");
        html.append("    body { font-family: Arial, sans-serif; color: white; background-color: #333; margin: 20px; }\n");
        html.append("    h1, h2, h3 { color: #4698e2; }\n");
        html.append("    .section { margin-bottom: 20px; }\n");
        html.append("    .disclaimer { color: #aaa; font-style: italic; border-top: 1px solid #555; padding-top: 10px; }\n");
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // 标题
        html.append("  <h1>").append(stockName).append(" 分析报告</h1>\n");

        // 处理报告内容 - 将文本转换为结构化HTML
        String[] sections = report.split("\n\n");
        for (String section : sections) {
            if (section.trim().isEmpty()) continue;

            if (section.toLowerCase().contains("风险") ||
                    section.toLowerCase().contains("免责声明") ||
                    section.toLowerCase().contains("disclaimer")) {
                html.append("  <div class=\"disclaimer\">\n");
                html.append("    <h3>免责声明</h3>\n");
                html.append("    <p>").append(section).append("</p>\n");
                html.append("  </div>\n");
            } else if (section.contains("：") || section.contains(":")) {
                String[] parts = section.split("[:：]", 2);
                if (parts.length > 1) {
                    html.append("  <div class=\"section\">\n");
                    html.append("    <h2>").append(parts[0].trim()).append("</h2>\n");
                    html.append("    <p>").append(parts[1].trim().replace("\n", "<br/>")).append("</p>\n");
                    html.append("  </div>\n");
                } else {
                    html.append("  <div class=\"section\">\n");
                    html.append("    <p>").append(section.replace("\n", "<br/>")).append("</p>\n");
                    html.append("  </div>\n");
                }
            } else {
                // 检查是否为标题
                if (section.length() < 50 && !section.contains("。")) {
                    html.append("  <h2>").append(section).append("</h2>\n");
                } else {
                    html.append("  <div class=\"section\">\n");
                    html.append("    <p>").append(section.replace("\n", "<br/>")).append("</p>\n");
                    html.append("  </div>\n");
                }
            }
        }

        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * 显示错误消息
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}