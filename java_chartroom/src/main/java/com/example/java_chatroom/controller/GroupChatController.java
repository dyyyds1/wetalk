package com.example.java_chatroom.controller;

import com.example.java_chatroom.model.GroupChat;
import com.example.java_chatroom.mapper.GroupChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GroupChatController {
    @Autowired
    private GroupChatMapper groupChatMapper;

    @PostMapping("/createGroupChat")
    public ResponseEntity<String> createGroupChat(List<Integer> friendIds,String groupName,int userId) {
        GroupChat groupChat=new GroupChat();
        try {
            groupChatMapper.createGroupChat(groupChat);
            return ResponseEntity.ok("群聊创建成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("创建群聊失败");
        }
    }
}
