package com.example.java_chatroom.mapper;

import com.example.java_chatroom.dto.DeleteGroupTimeDTO;
import com.example.java_chatroom.model.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface MessageMapper {
    //获取指定会话最后一条消息
    String getLastMessageBySessionId(int sessionId);
    //获取指定会话历史消息
    List<Message> getMessagesBySessionId(int sessionId);
    //插入消息到数据库中
    void add(Message message);

    Date findLastTimeBySessionId(int sessionId);

    int selectMessageId(int sessionId,int userId);
    //获取群聊指定会话最后一条消息且小于删除时间
    String getDeleteLastMessageBySessionId(DeleteGroupTimeDTO deleteGroupTimeDTO);
}
