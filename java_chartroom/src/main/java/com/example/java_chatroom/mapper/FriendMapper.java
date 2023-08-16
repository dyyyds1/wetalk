package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.Friend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;


import java.util.List;

@Mapper
public interface FriendMapper {

    List<Friend> getFriendsByUserId(int user_id);
}
