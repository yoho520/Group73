package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.ChatMessage;
import com.example.software.financeapp.service.ai.FinanceAIService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * AI聊天界面控制器
 */
public class AIChatController implements Initializable {

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private VBox chatMessagesBox;

    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox suggestedQuestionsBox;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    // AI服务
    private FinanceAIService aiService;

    // 聊天记录
    private ObservableList<ChatMessage> chatHistory = FXCollections.observableArrayList();

    // 当前输入的问题
    private StringProperty currentQuestion = new SimpleStringProperty("");

    // 日期格式化器
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化AI服务
        this.aiService = new FinanceAIService();

        // 初始化UI
        initializeUI();

        // 添加欢迎消息
        addAIMessage("您好！我是您的智能财务助手。请问有什么可以帮助您的吗？");

        // 显示推荐问题
        updateSuggestedQuestions(null);
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        // 设置聊天消息区域样式
        chatMessagesBox.setSpacing(10);
        chatMessagesBox.setPadding(new Insets(10));

        // 绑定输入框到属性
        messageField.textProperty().bindBidirectional(currentQuestion);

        // 设置输入框Enter键发送
        messageField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage(null);
                event.consume();
            }
        });

        // 监听聊天记录变化，自动滚动到底部
        chatHistory.addListener((javafx.collections.ListChangeListener.Change<? extends ChatMessage> c) -> {
            Platform.runLater(() -> {
                chatScrollPane.setVvalue(1.0);
            });
        });
    }

    /**
     * 处理发送消息按钮点击
     */
    @FXML
    private void handleSendMessage(ActionEvent event) {
        String question = currentQuestion.get().trim();

        if (question.isEmpty()) {
            return;
        }

        // 添加用户消息
        addUserMessage(question);

        // 清空输入框
        currentQuestion.set("");

        // 设置状态为"思考中"
        statusLabel.setText("思考中...");

        // 禁用发送按钮
        sendButton.setDisable(true);

        // 创建临时的AI回复消息框用于显示打字效果
        HBox messageBox = createEmptyAIMessageBox("正在思考...");
        int messageIndex = chatMessagesBox.getChildren().size();
        chatMessagesBox.getChildren().add(messageBox);

        // 使用CompletableFuture异步获取AI回答
        aiService.getAnswerAsync(question)
                .thenAccept(answer -> {
                    // 在UI线程中更新界面
                    Platform.runLater(() -> {
                        // 移除临时消息框
                        chatMessagesBox.getChildren().remove(messageIndex);

                        // 添加AI回复
                        addAIMessage(answer);

                        // 更新状态
                        statusLabel.setText("准备就绪");

                        // 启用发送按钮
                        sendButton.setDisable(false);

                        // 更新推荐问题
                        updateSuggestedQuestions(question);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // 移除临时消息框
                        chatMessagesBox.getChildren().remove(messageIndex);

                        statusLabel.setText("发生错误");
                        addAIMessage("抱歉，处理您的问题时出现了错误: " + ex.getMessage());

                        // 启用发送按钮
                        sendButton.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * 创建空的AI消息框，用于打字效果
     * @param placeholder 占位文本
     * @return 消息框
     */
    private HBox createEmptyAIMessageBox(String placeholder) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContainer = new VBox();
        messageContainer.setMaxWidth(chatScrollPane.getWidth() * 0.7);

        // 消息时间和来源
        Label sourceTimeLabel = new Label("AI助手 · " + LocalDateTime.now().format(timeFormatter));
        sourceTimeLabel.setTextFill(Color.GRAY);
        sourceTimeLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));

        // 消息内容
        TextFlow messageFlow = new TextFlow();
        messageFlow.setPadding(new Insets(8));
        messageFlow.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 10;");

        Text text = new Text(placeholder);
        text.setFill(Color.GRAY);
        messageFlow.getChildren().add(text);

        // 添加三点加载动画
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        progressIndicator.setStyle("-fx-progress-color: #808080;");

        HBox loadingBox = new HBox(5, messageFlow, progressIndicator);

        messageContainer.getChildren().addAll(sourceTimeLabel, loadingBox);
        messageBox.getChildren().add(messageContainer);

        return messageBox;
    }

    /**
     * 添加用户消息
     * @param message 消息内容
     */
    private void addUserMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage chatMessage = new ChatMessage(message, "用户", now, false);
        chatHistory.add(chatMessage);

        // 创建消息视图
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContainer = new VBox();
        messageContainer.setMaxWidth(chatScrollPane.getWidth() * 0.7);

        // 消息时间
        Label timeLabel = new Label(now.format(timeFormatter));
        timeLabel.setTextFill(Color.GRAY);
        timeLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));

        // 消息内容
        TextFlow messageFlow = new TextFlow();
        messageFlow.setPadding(new Insets(8));
        messageFlow.setStyle("-fx-background-color: #0078D7; -fx-background-radius: 10;");

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        messageFlow.getChildren().add(text);

        messageContainer.getChildren().addAll(messageFlow, timeLabel);
        messageContainer.setAlignment(Pos.CENTER_RIGHT);

        messageBox.getChildren().add(messageContainer);

        chatMessagesBox.getChildren().add(messageBox);

        // 滚动到底部
        scrollToBottom();
    }

    /**
     * 添加AI消息
     * @param message 消息内容
     */
    private void addAIMessage(String message) {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage chatMessage = new ChatMessage(message, "AI助手", now, true);
        chatHistory.add(chatMessage);

        // 创建消息视图
        HBox messageBox = new HBox();
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 0, 5, 0));

        VBox messageContainer = new VBox();
        messageContainer.setMaxWidth(chatScrollPane.getWidth() * 0.7);

        // 消息时间和来源
        Label sourceTimeLabel = new Label("AI助手 · " + now.format(timeFormatter));
        sourceTimeLabel.setTextFill(Color.GRAY);
        sourceTimeLabel.setFont(Font.font("System", FontWeight.NORMAL, 10));

        // 消息内容
        TextFlow messageFlow = new TextFlow();
        messageFlow.setPadding(new Insets(8));
        messageFlow.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 10;");

        // 处理消息中的换行
        String[] lines = message.split("\n");
        for (int i = 0; i < lines.length; i++) {
            Text text = new Text(lines[i]);
            text.setFill(Color.BLACK);
            messageFlow.getChildren().add(text);

            // 如果不是最后一行，添加换行
            if (i < lines.length - 1) {
                messageFlow.getChildren().add(new Text("\n"));
            }
        }

        messageContainer.getChildren().addAll(sourceTimeLabel, messageFlow);

        messageBox.getChildren().add(messageContainer);

        chatMessagesBox.getChildren().add(messageBox);

        // 滚动到底部
        scrollToBottom();
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    /**
     * 处理清空聊天记录按钮点击
     */
    @FXML
    private void handleClearChat(ActionEvent event) {
        // 清空聊天记录
        chatHistory.clear();
        chatMessagesBox.getChildren().clear();

        // 清空AI服务对话历史
        aiService.clearConversationHistory();

        // 添加欢迎消息
        addAIMessage("聊天记录已清空。有什么可以帮助您的吗？");

        // 更新推荐问题
        updateSuggestedQuestions(null);
    }

    /**
     * 更新推荐问题
     * @param lastQuestion 上一次提问，用于上下文相关推荐
     */
    private void updateSuggestedQuestions(String lastQuestion) {
        // 清空当前推荐
        suggestedQuestionsBox.getChildren().clear();

        // 根据对话历史推断用户兴趣
        String interestCategory = null;

        // 如果对话超过3轮，尝试推断兴趣
        if (chatHistory.size() >= 6) {  // 每轮包含用户和AI各一条消息
            interestCategory = aiService.inferUserInterests();
        }

        // 获取推荐问题
        List<String> recommendedQuestions = aiService.getRecommendedQuestions(interestCategory);

        // 添加推荐问题按钮
        for (String question : recommendedQuestions) {
            Button questionButton = new Button(question);
            questionButton.getStyleClass().add("suggested-question-button");
            questionButton.setOnAction(e -> {
                // 点击按钮时自动填入问题并发送
                currentQuestion.set(question);
                handleSendMessage(null);
            });

            // 设置自动换行
            questionButton.setWrapText(true);
            questionButton.setMaxWidth(250);

            suggestedQuestionsBox.getChildren().add(questionButton);
        }
    }

    /**
     * 处理帮助按钮点击
     */
    @FXML
    private void handleHelp(ActionEvent event) {
        // 显示帮助信息
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("AI助手使用帮助");
        alert.setHeaderText("如何使用AI财务助手");

        String content = "AI财务助手可以回答您关于财务管理的各种问题，包括：\n\n" +
                "• 预算管理\n" +
                "• 支出分析\n" +
                "• 储蓄规划\n" +
                "• 本地化财务适配\n" +
                "• 交易记录\n" +
                "• 账户管理\n" +
                "• 系统使用\n" +
                "• 财务建议\n\n" +
                "您可以直接输入问题，或点击下方的推荐问题。\n" +
                "AI助手会根据您的提问历史，推荐相关的问题。\n\n" +
                "智能财务助手由轩辕金融大模型提供支持，专业解答金融理财问题。";

        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 处理导出聊天记录按钮点击
     */
    @FXML
    private void handleExportChat(ActionEvent event) {
        StringBuilder chatLog = new StringBuilder();

        // 添加标题
        chatLog.append("财务AI助手聊天记录\n");
        chatLog.append("日期：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");

        // 添加聊天记录
        for (ChatMessage message : chatHistory) {
            chatLog.append(message.getSender()).append(" (")
                    .append(message.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("):\n")
                    .append(message.getContent()).append("\n\n");
        }

        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存聊天记录");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("文本文件", "*.txt"));
        fileChooser.setInitialFileName("财务AI助手聊天记录_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");

        // 显示保存对话框
        File file = fileChooser.showSaveDialog(chatScrollPane.getScene().getWindow());

        if (file != null) {
            try {
                // 写入文件
                Files.write(file.toPath(), chatLog.toString().getBytes());

                // 显示成功消息
                showInfoAlert("导出成功", "聊天记录已成功导出到：" + file.getAbsolutePath());
            } catch (IOException e) {
                showErrorAlert("导出失败", "无法保存文件：" + e.getMessage());
            }
        }
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