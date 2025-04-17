package com.example.software.financeapp.application;

import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.UserService;
// 添加导入
import com.example.software.financeapp.service.ai.ClassificationService;
import javafx.stage.Stage;

/**
 * 应用程序上下文
 * 用于在应用程序各部分之间共享状态和服务
 */
public class AppContext {

    private static AppContext instance;
    // 在类中添加字段
    private ClassificationService classificationService;
    // 主窗口引用
    private Stage primaryStage;

    // 当前登录用户
    private User currentUser;

    // 服务引用
    private ApiService apiService;
    private UserService userService;

    // 应用程序配置
    private String apiBaseUrl;
    private String appVersion = "1.0.0";
    private boolean darkMode = false;

    private AppContext() {
        // 私有构造函数，防止外部实例化
        initialize();
    }

    /**
     * 获取应用上下文单例实例
     * @return AppContext实例
     */
    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    /**
     * 初始化应用上下文
     */
    private void initialize() {
        // 加载配置
        loadConfiguration();

        // 初始化服务
        initializeServices();
    }

    /**
     * 加载应用配置
     */
    private void loadConfiguration() {
        // 从配置文件加载设置
        try {
            // 默认API基础URL
            apiBaseUrl = "http://localhost:8080/api";

            // TODO: 从properties文件加载实际配置

        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 初始化服务
     */
    private void initializeServices() {
        apiService = new ApiService(apiBaseUrl);
        userService = new UserService(apiService);
        this.classificationService = new ClassificationService(apiService);
    }
// 添加getter方法
    /**
     * 获取分类服务
     * @return 分类服务
     */
    public ClassificationService getClassificationService() {
        return classificationService;
    }
    /**
     * 设置主窗口引用
     * @param primaryStage 主窗口Stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * 获取主窗口引用
     * @return 主窗口Stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * 设置当前用户
     * @param user 用户
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 获取API服务
     * @return API服务
     */
    public ApiService getApiService() {
        return apiService;
    }

    /**
     * 获取用户服务
     * @return 用户服务
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * 获取应用版本
     * @return 应用版本
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * 检查是否为暗黑模式
     * @return 是否为暗黑模式
     */
    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * 切换暗黑/明亮模式
     * @param darkMode 是否为暗黑模式
     */
    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;

        // 应用主题变化
        if (primaryStage != null && primaryStage.getScene() != null) {
            String themeCss = darkMode
                    ? "/css/dark-theme.css"
                    : "/css/light-theme.css";

            // 移除当前主题
            primaryStage.getScene().getStylesheets().removeIf(
                    css -> css.contains("theme"));

            // 添加新主题
            primaryStage.getScene().getStylesheets().add(
                    getClass().getResource(themeCss).toExternalForm());
        }
    }

    /**
     * 保存用户配置
     */
    public void saveUserPreferences() {
        // TODO: 保存用户首选项到配置文件
        System.out.println("保存用户偏好设置...");
    }

    /**
     * 清除登录状态
     */
    public void logout() {
        currentUser = null;
        // 执行其他登出操作
    }
}