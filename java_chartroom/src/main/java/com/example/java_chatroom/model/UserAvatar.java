package com.example.java_chatroom.model;

import lombok.Data;

@Data
public class UserAvatar {
    private int user_avatar_id;
    private int userId; // 关联用户的 ID
    private String username; // 用户名
    private String avatar_path; // 头像文件路径

    // 构造函数、Getter 和 Setter 方法
}