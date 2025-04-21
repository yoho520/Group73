package com.example.software.financeapp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AI服务配置类
 * 用于管理API密钥和配置信息
 */
public class AIServiceConfig {
    private static final String CONFIG_FILE = "application.properties";

    private String apiKey;
    private String secretKey;
    private boolean enableLocalFallback;
    private float temperature;
    private int maxHistoryRounds;

    /**
     * 构造函数，从配置文件加载设置
     */
    public AIServiceConfig() {
        // 从属性文件加载配置
        loadFromProperties();

        // 如果属性文件没有配置，从环境变量加载
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("XUANYUAN_API_KEY");
        }

        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = System.getenv("XUANYUAN_SECRET_KEY");
        }

        // 设置默认值
        if (temperature <= 0 || temperature > 1.0) {
            temperature = 0.7f;
        }

        if (maxHistoryRounds <= 0) {
            maxHistoryRounds = 5;
        }
    }

    /**
     * 从属性文件加载配置
     */
    private void loadFromProperties() {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);

                this.apiKey = properties.getProperty("xuanyuan.api.key");
                this.secretKey = properties.getProperty("xuanyuan.api.secret");
                this.enableLocalFallback = Boolean.parseBoolean(properties.getProperty("xuanyuan.api.enable-local-fallback", "true"));

                String tempStr = properties.getProperty("xuanyuan.api.temperature");
                if (tempStr != null && !tempStr.isEmpty()) {
                    try {
                        this.temperature = Float.parseFloat(tempStr);
                    } catch (NumberFormatException e) {
                        this.temperature = 0.7f;
                    }
                } else {
                    this.temperature = 0.7f;
                }

                String historyRoundsStr = properties.getProperty("xuanyuan.api.max-history-rounds");
                if (historyRoundsStr != null && !historyRoundsStr.isEmpty()) {
                    try {
                        this.maxHistoryRounds = Integer.parseInt(historyRoundsStr);
                    } catch (NumberFormatException e) {
                        this.maxHistoryRounds = 5;
                    }
                } else {
                    this.maxHistoryRounds = 5;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public boolean isEnableLocalFallback() {
        return enableLocalFallback;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getMaxHistoryRounds() {
        return maxHistoryRounds;
    }
}