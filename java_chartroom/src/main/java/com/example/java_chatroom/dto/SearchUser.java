package com.example.java_chatroom.dto;

import lombok.Data;

@Data
public class SearchUser {
    private int userId;

    private String username;

    private boolean status;
}
