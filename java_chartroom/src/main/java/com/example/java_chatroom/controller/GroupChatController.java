package com.example.java_chatroom.controller;

import com.example.java_chatroom.dto.GroupChatRequest;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.model.GroupChat;
import com.example.java_chatroom.mapper.GroupChatMapper;
import com.example.java_chatroom.model.MessageSession;
import com.example.java_chatroom.model.MessageSessionUserItem;
import com.example.java_chatroom.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class GroupChatController {
    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    @PostMapping("/createGroupChat")
    @ResponseBody
    public GroupChat createGroupChat(@RequestBody GroupChatRequest request, @SessionAttribute("user") User user) {
        List<Integer> friendIds = request.getFriendIds();
        String groupName = request.getGroupName();
        GroupChat groupChat=new GroupChat();
        friendIds.add(user.getUser_id());
        MessageSession messageSession=new MessageSession();
        //插入群聊，并获取sessionId
        messageSessionMapper.addMessageSession(messageSession);
        //把friendIds的所有id都插入session_user
        for (int userId : friendIds){
            addMessageSession(userId,messageSession);
        }
        groupChat.setCreatedBy(user.getUser_id());
        groupChat.setSessionId(messageSession.getSessionId());
        groupChat.setGroupName(groupName);
        try {
            groupChatMapper.createGroupChat(groupChat);
            return groupChat;
        } catch (Exception e) {
            return null;
        }
    }

    public void addMessageSession(int userId, MessageSession messageSession){
        //给message_session_user表插入记录
        MessageSessionUserItem item=new MessageSessionUserItem();
        item.setSessionId(messageSession.getSessionId());
        item.setUserId(userId);
        messageSessionMapper.addMessageSessionUser(item);
    }

    @GetMapping("/isGroupChat")
    public Map<String, Boolean> checkGroupChat(@RequestParam Integer sessionId) {
        int count = groupChatMapper.countGroupChatBySessionId(sessionId);
        boolean isGroupChat = count > 0;
        Map<String, Boolean> result = new HashMap<>();
        result.put("isGroupChat", isGroupChat);
        return result;
    }
}
