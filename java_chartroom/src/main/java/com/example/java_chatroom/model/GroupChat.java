package com.example.java_chatroom.model;


import lombok.Data;

import java.util.Date;

@Data
public class GroupChat {
    private int groupId;
    //群聊名字
    private String groupName;
    //创建时间
    private Date creationTime;
    //群主
    private int createdBy;
    //会话id
    private int sessionId;


}