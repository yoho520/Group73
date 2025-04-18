package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 主视图控制器
 * 负责管理应用程序的主窗口和导航
 */
public class MainController implements Initializable {

    @FXML
    private BorderPane mainLayout;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label userNameLabel;

    @FXML
    private ToggleButton dashboardToggle;

    @FXML
    private ToggleButton transactionsToggle;

    @FXML
    private ToggleButton budgetToggle;

    @FXML
    private ToggleButton reportsToggle;

    // 添加新的导航按钮字段
    @FXML
    private ToggleButton familyFinanceToggle;

    @FXML
    private ToggleButton investmentToggle;

    @FXML
    private ToggleButton securityToggle;

    @FXML
    private ToggleButton aiAssistantToggle;

    @FXML
    private ToggleButton settingsToggle;

    @FXML
    private ToggleGroup navigationGroup;

    @FXML
    private ToggleButton darkModeToggle;

    // 应用程序上下文
    private final AppContext appContext = AppContext.getInstance();

    /**
     * 初始化控制器
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 设置导航切换按钮组
        if (navigationGroup == null) {
            navigationGroup = new ToggleGroup();
            dashboardToggle.setToggleGroup(navigationGroup);
            transactionsToggle.setToggleGroup(navigationGroup);
            budgetToggle.setToggleGroup(navigationGroup);
            reportsToggle.setToggleGroup(navigationGroup);
            // 将新按钮添加到导航组
            familyFinanceToggle.setToggleGroup(navigationGroup);
            investmentToggle.setToggleGroup(navigationGroup);
            securityToggle.setToggleGroup(navigationGroup);
            aiAssistantToggle.setToggleGroup(navigationGroup);
            settingsToggle.setToggleGroup(navigationGroup);
        }

        // 监听导航按钮组选择变化
        navigationGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            // 确保总是有一个按钮被选中
            if (newValue == null) {
                oldValue.setSelected(true);
                return;
            }

            // 根据选中的按钮加载相应的视图
            ToggleButton selected = (ToggleButton) newValue;
            loadView(selected.getId());
        });

        // 设置暗黑模式切换按钮状态
        darkModeToggle.setSelected(appContext.isDarkMode());

        // 默认选中仪表板
        dashboardToggle.setSelected(true);
        loadView("dashboardToggle");

        // 检查登录状态
        checkLoginStatus();
    }

    /**
     * 检查登录状态
     */
    private void checkLoginStatus() {
        User currentUser = appContext.getCurrentUser();

        if (currentUser == null) {
            // 显示登录界面
            showLoginView();
        } else {
            // 更新UI显示用户信息
            updateUserInfo(currentUser);
        }
    }

    /**
     * 显示登录视图
     */
    private void showLoginView() {
        // 跳过加载登录视图，直接模拟一个已登录用户
        User demoUser = User.builder()
                .id(1L)
                .username("demo")
                .fullName("演示用户")
                .build();

        // 设置当前用户
        appContext.setCurrentUser(demoUser);

        // 更新用户信息显示
        updateUserInfo(demoUser);

        // 加载主界面
        dashboardToggle.setSelected(true);
        loadView("dashboardToggle");
    }

    /**
     * 更新用户信息显示
     * @param user 当前用户
     */
    private void updateUserInfo(User user) {
        // 更新用户名标签
        userNameLabel.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
    }

    /**
     * 根据ID加载相应的视图
     * @param viewId 视图ID
     */
    private void loadView(String viewId) {
        // 清空内容区域
        contentArea.getChildren().clear();

        try {
            // 根据视图ID加载相应的FXML
            String fxmlPath;
            switch (viewId) {
                case "dashboardToggle":
                    //fxmlPath = "/view/dashboard.fxml";
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/savings_plan.fxml";
                    break;
                case "transactionsToggle":
                    fxmlPath = "/view/transactions.fxml";
                    break;
                case "budgetToggle":
                    // 更新为预算洞察视图
                    fxmlPath = "/view/budget_insights.fxml";
                    break;
                case "reportsToggle":
                    // 更新为支出分析视图
                    fxmlPath = "/view/spending_analysis.fxml";
                    break;
                case "familyFinanceToggle":
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/transactions.fxml";
                    break;
                case "investmentToggle":
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/localization_settings.fxml";
                    break;
                case "securityToggle":
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/transactions.fxml";
                    break;
                case "aiAssistantToggle":
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/ai_chat.fxml";
                    break;
                case "settingsToggle":
                    //fxmlPath = "/view/settings.fxml";
                    // 暂时使用交易视图代替
                    fxmlPath = "/view/transactions.fxml";
                    break;
                default:
                    fxmlPath = "/view/transactions.fxml";
            }

            // 加载视图
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            showErrorAlert("加载视图失败", e.getMessage());
        }
    }

    /**
     * 处理暗黑模式切换
     */
    @FXML
    private void handleDarkModeToggle(ActionEvent event) {
        boolean darkMode = darkModeToggle.isSelected();
        appContext.setDarkMode(darkMode);
    }

    /**
     * 处理退出应用程序
     */
    @FXML
    private void handleExit(ActionEvent event) {
        // 保存用户设置
        //appContext.saveUserPreferences();

        // 退出应用程序
        Platform.exit();
    }

    /**
     * 处理注销
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // 清除登录状态
        appContext.logout();

        // 显示登录界面
        showLoginView();
    }

    /**
     * 显示错误提示对话框
     * @param title 标题
     * @param message 消息
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}