package com.example.java_chatroom.dto;

import lombok.Data;

// 表示一个响应
@Data
public class MessageResponse {
    private String type = "message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private int toUserId;
    private String content;

    private int noReadCount;
    //是否是群聊 1是群聊，0不是群聊
    private int isGroupChat;
    //群聊名字
    private String groupName;

}
