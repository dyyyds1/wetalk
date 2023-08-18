package com.example.java_chatroom.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupChatRequest {
    private List<Integer> friendIds;
    private String groupName;

    // Getters and setters
}
