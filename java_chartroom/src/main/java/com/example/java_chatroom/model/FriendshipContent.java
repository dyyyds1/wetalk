package com.example.java_chatroom.model;

import lombok.Data;

//添加好友时的备注信息
@Data
public class FriendshipContent {
    private int userId;
    private String username;
    private int friendId;

    private String content;
}
