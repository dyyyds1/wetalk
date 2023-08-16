package com.example.java_chatroom.model;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    private int messageId;
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;
    private Date postTime;

}
