package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.GroupChat;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface GroupChatMapper {
    //创建群聊
    void createGroupChat(GroupChat groupChat);
    //判断是否为群聊的时候用
    int countGroupChatBySessionId(Integer sessionId);
    //根据id获取群聊信息
    GroupChat getGroupChatBySessionId(Integer sessionId);

    void exitGroup(int sessionId,int userId);

    //0存在，1不存在
    int isInGroupChat(int sessionId,int userId);

    void deleteGroupUser(int sessionId, int userId);

    Date getDeleteGroupTime(int sessionId,int userId);

    List<Integer> selectAllUsers(int sessionId);

    void inviteFriend(int sessionId,int friendId);
}