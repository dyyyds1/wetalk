package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.GroupChat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupChatMapper {
    void createGroupChat(GroupChat groupChat);
}