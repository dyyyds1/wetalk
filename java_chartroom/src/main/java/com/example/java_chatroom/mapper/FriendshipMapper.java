package com.example.java_chatroom.mapper;

import com.example.java_chatroom.dto.FriendshipAndContentDTO;
import com.example.java_chatroom.dto.SearchUser;
import com.example.java_chatroom.dto.UserTemp;
import com.example.java_chatroom.model.Friend;
import com.example.java_chatroom.model.Friendship;
import com.example.java_chatroom.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendshipMapper {
  int addFriendship(Friendship friendship);
  void updateFriendshipStatus(int applierId,int userId);
  void deleteFriendship(int userId,int friendId);
  List<SearchUser> searchUsersByUsername(UserTemp userTemp);
  Friendship getFriendshipById(int friendshipId);
  Friendship getFriendshipByUserIds(@Param("userId") int userId, @Param("friendId") int friendId);
//  List<Friendship> getFriendshipsByUserId(int userId);

  List<Integer> getUserIdsByUsername(String username);
  Boolean getFriendshipStatus(int userId,int friendId);

  Boolean isHaveApply(int userId);

  List<FriendshipAndContentDTO>  getAppliersList(int userId);
  void deleteFriend(int friendId,int userId);

  Integer getFriendId(String friendName);

}