package com.example.java_chatroom.mapper;

import com.example.java_chatroom.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    //添加用户->注册
    int insert(User user);
    //查询用户信息->登录
    User selectByName(String username);

    String selectUsernameById(int userId);
}
