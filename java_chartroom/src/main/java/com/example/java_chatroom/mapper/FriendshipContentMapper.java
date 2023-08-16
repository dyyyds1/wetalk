package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.FriendshipContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendshipContentMapper {
    void addContent(FriendshipContent friendshipContent);

    void deleteContent(int friendId,int userId);
}
