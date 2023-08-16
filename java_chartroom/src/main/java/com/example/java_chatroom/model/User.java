package com.example.java_chatroom.model;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private int user_id;
    private String username="";
    private String password="";

    private Date created_at;
    private Date updated_at;
}
