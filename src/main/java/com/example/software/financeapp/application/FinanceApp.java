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

    private static final String APP_TITLE = "财务管理助手";
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        try {
            // 加载主视图
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
            Parent root = loader.load();

            // 创建场景
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // 添加默认样式表
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // 添加新的应用样式表（用于AI分类功能）
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

            // 设置应用图标 (如果有的话)
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app-icon.png")));

            // 配置和显示主窗口
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            // 保存主窗口引用
            AppContext.getInstance().setPrimaryStage(primaryStage);

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