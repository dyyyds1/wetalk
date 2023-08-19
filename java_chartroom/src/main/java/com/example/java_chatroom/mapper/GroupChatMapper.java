package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.GroupChat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupChatMapper {
    //创建群聊
    void createGroupChat(GroupChat groupChat);
    //判断是否为群聊的时候用
    int countGroupChatBySessionId(Integer sessionId);
    //根据id获取群聊信息
    GroupChat getGroupChatBySessionId(Integer sessionId);

    void exitGroup(int sessionId,int userId);
}