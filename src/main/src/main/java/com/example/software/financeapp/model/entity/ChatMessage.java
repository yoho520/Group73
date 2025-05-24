package com.example.software.financeapp.model.entity;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 */
public class ChatMessage {
    private String content;         // 消息内容
    private String sender;          // 发送者
    private LocalDateTime timestamp; // 时间戳
    private boolean isAI;           // 是否是AI消息

    public ChatMessage() {
    }

    public ChatMessage(String content, String sender, LocalDateTime timestamp, boolean isAI) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isAI = isAI;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean isAI) {
        this.isAI = isAI;
    }
}