package com.example.software.financeapp.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * 财务管理应用程序的主类
 */
public class FinanceApp extends Application {

    private static final String APP_TITLE = "Financial Assistant";
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;
    private static final double LOGIN_WIDTH = 600;
    private static final double LOGIN_HEIGHT = 500;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 保存主窗口引用
            AppContext.getInstance().setPrimaryStage(primaryStage);

            // 加载登录视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load();

            // 创建场景
            Scene scene = new Scene(root, LOGIN_WIDTH, LOGIN_HEIGHT);

            // 添加样式表
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

            // 配置和显示登录窗口
            primaryStage.setTitle(APP_TITLE + " - 登录");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("无法加载登录视图: " + e.getMessage());
        }
    }

    /**
     * 加载主窗口
     * @param
     */
    public static void loadMainWindow() {
        try {
            // 获取舞台
            Stage primaryStage = AppContext.getInstance().getPrimaryStage();

            // 加载主视图
            FXMLLoader loader = new FXMLLoader(FinanceApp.class.getResource("/view/main.fxml"));
            Parent root = loader.load();

            // 创建场景
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // 添加样式表
            scene.getStylesheets().add(FinanceApp.class.getResource("/css/styles.css").toExternalForm());
            scene.getStylesheets().add(FinanceApp.class.getResource("/styles/application.css").toExternalForm());

            // 配置和显示主窗口
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("无法加载主视图: " + e.getMessage());
        }
    }

    /**
     * 应用程序入口点
     */
    public static void main(String[] args) {
        // 启动JavaFX应用
        launch(args);
    }

    /**
     * 应用程序关闭前的清理工作
     */
    @Override
    public void stop() {
        System.out.println("正在关闭应用...");

        // 执行清理操作
        // 例如：保存用户配置，关闭连接等

        System.out.println("应用已安全关闭");
    }
}