package com.example.java_chatroom.controller;

import com.example.java_chatroom.dto.SessionLastTimeDTO;
import com.example.java_chatroom.mapper.MessageMapper;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.model.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RestController
public class MessageController {
    @Resource
    private MessageMapper messageMapper;

    @Resource
    private MessageSessionMapper messageSessionMapper;

    @GetMapping("/message")
    public Object getMessage(int sessionId){
        List<Message> messages=messageMapper.getMessagesBySessionId(sessionId);
        //逆置消息
        Collections.reverse(messages);
        return messages;
    }

    @GetMapping("/getDeleteDate")
    public Object getDeleteDate(int sessionId,int userId){
        int isDelete= messageSessionMapper.isHaveDeleteData(sessionId,userId);
        SessionLastTimeDTO sessionLastTimeDTO=new SessionLastTimeDTO();

        if (isDelete==0){
             sessionLastTimeDTO.setLastTime(new Date(0));
             return sessionLastTimeDTO;
        }
        Date lastTime=messageSessionMapper.getDeleteLastTime(sessionId,userId);
        sessionLastTimeDTO.setLastTime(lastTime);
        return sessionLastTimeDTO;
    }
}
