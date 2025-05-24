package com.example.software.financeapp.service.ai;

import com.example.software.financeapp.model.entity.ChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 轩辕大模型API客户端
 * 基于百度XuanYuan-70B-Chat-4bit模型实现
 */
public class XuanYuanAIClient {
    // API接口URL
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String API_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/xuanyuan_70b_chat";

    // 应用凭证
    private final String apiKey;
    private final String secretKey;

    // 访问令牌
    private String accessToken;
    private long tokenExpireTime;

    /**
     * 构造函数
     * @param apiKey 应用API Key
     * @param secretKey 应用Secret Key
     */
    public XuanYuanAIClient(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.accessToken = null;
        this.tokenExpireTime = 0;
    }

    /**
     * 发送对话请求
     * @param messages 对话消息列表
     * @param stream 是否使用流式接口
     * @param temperature 温度参数
     * @return AI回答
     * @throws IOException IO异常
     */
    public String sendQuery(List<JSONObject> messages, boolean stream, float temperature) throws IOException {
        // 确保有访问令牌
        ensureAccessToken();

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("messages", messages);
        requestBody.put("stream", stream);

        if (temperature > 0 && temperature <= 1.0) {
            requestBody.put("temperature", temperature);
        }

        // 发送请求
        String apiUrlWithToken = API_URL + "?access_token=" + accessToken;
        HttpURLConnection connection = createConnection(apiUrlWithToken, "POST");

        // 写入请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // 处理响应
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            if (stream) {
                return handleStreamResponse(connection);
            } else {
                return handleSyncResponse(connection);
            }
        } else {
            throw new IOException("API request failed: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     * 发送对话请求(简化版)
     * @param question 用户问题
     * @return AI回答
     * @throws IOException IO异常
     */
    public String sendQuery(String question) throws IOException {
        List<JSONObject> messages = new ArrayList<>();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);

        return sendQuery(messages, false, 0.7f);
    }

    /**
     * 发送多轮对话请求
     * @param conversationHistory 对话历史
     * @return AI回答
     * @throws IOException IO异常
     */
    public String sendConversationQuery(List<ChatMessage> conversationHistory) throws IOException {
        List<JSONObject> messages = new ArrayList<>();

        for (ChatMessage message : conversationHistory) {
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("role", message.isAI() ? "assistant" : "user");
            jsonMessage.put("content", message.getContent());
            messages.add(jsonMessage);
        }

        return sendQuery(messages, false, 0.7f);
    }

    /**
     * 异步发送对话请求
     * @param question 用户问题
     * @return 包含AI回答的CompletableFuture
     */
    public CompletableFuture<String> sendQueryAsync(String question) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendQuery(question);
            } catch (IOException e) {
                throw new RuntimeException("Failed to get AI response: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 异步发送多轮对话请求
     * @param conversationHistory 对话历史
     * @return 包含AI回答的CompletableFuture
     */
    public CompletableFuture<String> sendConversationQueryAsync(List<ChatMessage> conversationHistory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendConversationQuery(conversationHistory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to get AI response: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 确保有有效的访问令牌
     * @throws IOException IO异常
     */
    private void ensureAccessToken() throws IOException {
        long currentTime = System.currentTimeMillis();

        // 如果令牌为空或已过期，则获取新令牌
        if (accessToken == null || currentTime >= tokenExpireTime) {
            refreshAccessToken();
        }
    }

    /**
     * 刷新访问令牌
     * @throws IOException IO异常
     */
    private void refreshAccessToken() throws IOException {
        String url = TOKEN_URL + "?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
        HttpURLConnection connection = createConnection(url, "GET");

        // 处理响应
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // 解析响应
                JSONObject jsonResponse = new JSONObject(response.toString());
                accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getInt("expires_in");

                // 设置过期时间(提前5分钟过期，以确保安全)
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
            }
        } else {
            throw new IOException("Failed to get access token: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
    }

    /**
     * 处理同步响应
     * @param connection HTTP连接
     * @return AI回答
     * @throws IOException IO异常
     */
    private String handleSyncResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // 打印完整响应以便调试
            String responseStr = response.toString();
            System.out.println("API Response: " + responseStr);

            // 解析响应
            JSONObject jsonResponse = new JSONObject(responseStr);

            // 检查是否包含错误信息
            if (jsonResponse.has("error_code")) {
                int errorCode = jsonResponse.getInt("error_code");
                String errorMsg = jsonResponse.optString("error_msg", "Unknown error");
                throw new IOException("API Error: " + errorCode + " - " + errorMsg);
            }

            // 安全地获取result字段
            if (!jsonResponse.has("result")) {
                System.out.println("Warning: Response does not contain 'result' field: " + responseStr);
                // 返回整个响应作为备用
                return "API返回了一个没有预期结果的响应。完整响应: " + responseStr;
            }

            // 检查是否需要清除历史
            boolean needClearHistory = jsonResponse.optBoolean("need_clear_history", false);
            if (needClearHistory) {
                System.out.println("Warning: The conversation contains sensitive content and should be cleared.");
            }

            return jsonResponse.getString("result");
        }
    }

    /**
     * 处理流式响应
     * @param connection HTTP连接
     * @return 完整的AI回答
     * @throws IOException IO异常
     */
    private String handleStreamResponse(HttpURLConnection connection) throws IOException {
        StringBuilder fullResponse = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonStr = line.substring(6); // 移除 "data: " 前缀
                    JSONObject chunk = new JSONObject(jsonStr);

                    if (chunk.has("result")) {
                        String partialResult = chunk.getString("result");
                        fullResponse.append(partialResult);

                        // 可以在这里添加流式处理的回调逻辑
                    }

                    // 检查是否是最后一个块
                    boolean isEnd = chunk.optBoolean("is_end", false);
                    if (isEnd) {
                        break;
                    }
                }
            }
        }

        return fullResponse.toString();
    }

    /**
     * 创建HTTP连接
     * @param urlString URL字符串
     * @param method HTTP方法
     * @return HTTP连接
     * @throws IOException IO异常
     */
    private HttpURLConnection createConnection(String urlString, String method) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        return connection;
    }
}