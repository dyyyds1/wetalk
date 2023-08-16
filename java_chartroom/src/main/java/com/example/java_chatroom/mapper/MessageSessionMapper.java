package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.Friend;
import com.example.java_chatroom.model.MessageSession;
import com.example.java_chatroom.model.MessageSessionUserItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageSessionMapper {
    //1. 根据userId获取到该用户都在哪些会话中存在，返回结果是一组sessionId
    List<Integer> getSessionIdByUserId(int userId);
    //2. 根据sessionId再来查询这个会话都包含了哪些用户，（除去最初的自己）
    List<Friend> getFriendsBySessionId(int sessionId, int selfUserId);
    //3. 新增一个会话记录，返回会话的id
    int addMessageSession(MessageSession messageSession);
    //4. 给message_session_user表也增加对应的记录
    void addMessageSessionUser(MessageSessionUserItem messageSessionUserItem);

    void markMessageSessionUserAsDeleted(int sessionId,int userId);

    int isHaveDeleteData(int sessionId,int userId);

    void addDeleteData(int sessionId,int userId);

    void updateDeleteData(int sessionId,int userId);

    Date getDeleteLastTime(int sessionId,int userId);

    List<Integer> getSessionIdsByUserId(int userId);

    List<Integer> getSessionIdsByFriendId(int friendId);

    void markMessageSessionUserAsRestore(int sessionId, int userId);

    //消息已读未读模块
    int countOfNoRead(int sessionId, int userId);

    void addNoReadMessage(int messageId, int userId);

    void setReadMessage(int sessionId, int userId);

    void addReadMessage(int messageId, int userId);
}
