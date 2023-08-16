package com.example.java_chatroom.model;

import lombok.Data;


import java.util.Date;
@Data
public class Friendship {
  private int friendship_id;
  private int user_id;

  private int friend_id;
  private boolean status;

  private Date created_at;

  private Date updated_at;


  // Getters and setters
}