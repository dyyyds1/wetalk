package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.UserAvatar;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserAvatarMapper {

    UserAvatar getUserAvatarByUserId(int userId);

    void insertUserAvatar(UserAvatar userAvatar);

    void deleteUserAvatar(int userId);

    void updateAvatar(String path,int userId);
}