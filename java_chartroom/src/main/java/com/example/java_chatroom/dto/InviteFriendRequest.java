package com.example.java_chatroom.dto;

import lombok.Data;

import java.util.List;

@Data
public class InviteFriendRequest {
    private int sessionId;
    private List<Integer> friendIds;
}
