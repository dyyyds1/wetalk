package com.example.java_chatroom.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MessageSession {
    private int sessionId;
    private List<Friend> friends;
    private String lastMessage;

    private int countNoRead;
    //判断是否是群聊 1为群聊，0不为群聊
    private int isGroupChat;
    //群名字
    private String GroupChatName;
    //群主id
    private int createBy;

    //群人数
    private int groupersCount;

    private Date lastTime;
}
