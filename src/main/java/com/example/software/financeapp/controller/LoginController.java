package com.example.software.financeapp.controller;

import com.example.software.financeapp.application.AppContext;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.service.ApiService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private ApiService apiService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        apiService = AppContext.getInstance().getApiService();

        // 让错误标签初始不可见
        errorLabel.setVisible(false);

        // 为密码框添加回车键事件
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("用户名和密码不能为空");
            return;
        }

        try {
            // 调用API服务进行登录验证
            User user = apiService.login(username, password);

            if (user != null) {
                // 登录成功，保存用户到应用上下文
                AppContext.getInstance().setCurrentUser(user);

                // 在同一窗口中切换到主界面
                loadMainView();
            } else {
                // 登录失败
                showError("用户名或密码错误");
            }
        } catch (Exception e) {
            showError("登录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void loadMainView() {
        try {
            // 获取当前窗口
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // 加载主视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Parent mainRoot = loader.load();

            // 创建新场景
            Scene mainScene = new Scene(mainRoot, 1200, 800);

            // 添加样式表
            mainScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            mainScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

            // 设置窗口的新场景
            stage.setScene(mainScene);
            stage.setTitle("Financial Assistant");
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (IOException e) {
            showError("无法加载主界面: " + e.getMessage());
            e.printStackTrace();
        }
    }
}