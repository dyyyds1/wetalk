package com.example.java_chatroom.controller;

import com.example.java_chatroom.mapper.FriendMapper;
import com.example.java_chatroom.model.Friend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController {
    @Autowired
    private FriendMapper friendMapper;
    @GetMapping("/friends")
    public List<Friend> getFriends(int user_id) {
        return friendMapper.getFriendsByUserId(user_id);
    }
}
