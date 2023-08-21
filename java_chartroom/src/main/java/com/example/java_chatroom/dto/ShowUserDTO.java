package com.example.java_chatroom.dto;

import lombok.Data;

@Data
public class ShowUserDTO {
    private int userId;
    private String username;
    private String avatar_path;
}
