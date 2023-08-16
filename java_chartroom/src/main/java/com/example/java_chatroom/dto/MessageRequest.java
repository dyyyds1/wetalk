package com.example.java_chatroom.dto;

import lombok.Data;

// 表示一个消息请求
@Data
public class MessageRequest {
    private String type = "message";
    private int sessionId;
    private String content;
    private int toUserId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}