package com.example.java_chatroom.controller;

import com.example.java_chatroom.dto.FriendshipAndContentDTO;
import com.example.java_chatroom.dto.SearchUser;

import com.example.java_chatroom.dto.UserTemp;
import com.example.java_chatroom.mapper.FriendshipContentMapper;
import com.example.java_chatroom.mapper.FriendshipMapper;
import com.example.java_chatroom.mapper.MessageSessionMapper;
import com.example.java_chatroom.mapper.UserMapper;
import com.example.java_chatroom.model.ApiResponse;
import com.example.java_chatroom.model.Friend;
import com.example.java_chatroom.model.Friendship;
import com.example.java_chatroom.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FriendshipController {

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendshipContentMapper friendshipContentMapper;

    @Autowired
    private MessageSessionMapper messageSessionMapper;

    public int addFriendship(Friendship friendship) {
        // 先查询是否已存在相同的好友关系
        Friendship existingFriendship = friendshipMapper.getFriendshipByUserIds(friendship.getUser_id(), friendship.getFriend_id());
        if (existingFriendship == null) {
            // 如果不存在相同的好友关系，则添加好友关系
            friendship.setStatus(true); // 设置初始状态为待处理
            friendshipMapper.addFriendship(friendship);
        } else if (existingFriendship.isStatus()){
            System.out.println("已经申请过啦");
            return 0;
        } else {
            System.out.println("好友关系已经存在啦");
            return -1;
        }
        return 1;
    }

    @PutMapping("/acceptFriendship")
    public FriendshipAndContentDTO acceptFriendship(int applierId,int userId) {
        friendshipMapper.updateFriendshipStatus(applierId,userId);
        String username=userMapper.selectUsernameById(applierId);
        FriendshipAndContentDTO friendshipAndContentDTO=new FriendshipAndContentDTO();
        friendshipAndContentDTO.setContent("Success");
        friendshipAndContentDTO.setUsername(username);
        friendshipAndContentDTO.setUserId(applierId);
        List<Integer> sessionIds1=messageSessionMapper.getSessionIdsByUserId(userId);
        List<Integer> sessionIds2=messageSessionMapper.getSessionIdsByFriendId(applierId);
        for (int sessionId : sessionIds1){
            for (int i = 0; i < sessionIds2.size(); i++) {
                if (sessionId==sessionIds2.get(i)){
                    messageSessionMapper.markMessageSessionUserAsRestore(sessionId,userId);
                    messageSessionMapper.markMessageSessionUserAsRestore(sessionId,applierId);
                }
            }
        }

        return friendshipAndContentDTO;
    }


    @PostMapping("/deleteFriendship")
    public FriendshipAndContentDTO deleteFriendship(int userId,int friendId) {
        friendshipMapper.deleteFriendship(userId,friendId);
        FriendshipAndContentDTO friendshipAndContentDTO=new FriendshipAndContentDTO();
        friendshipContentMapper.deleteContent(userId,friendId);
        friendshipAndContentDTO.setContent("Success");
        return friendshipAndContentDTO;
    }

    @PostMapping("/deleteFriend")
    public FriendshipAndContentDTO deleteFriend(String username,int userId){
        User friend=userMapper.selectByName(username);
        if (friend != null) {
            int friendId = friend.getUser_id();
            friendshipMapper.deleteFriend(friendId, userId);
            friendshipContentMapper.deleteContent(friendId, userId);

            FriendshipAndContentDTO friendshipAndContentDTO = new FriendshipAndContentDTO();
            friendshipAndContentDTO.setContent("Success");
            return friendshipAndContentDTO;
        } else {
            // 处理 friend 为 null 的情况，例如返回错误信息或其他处理
            // 例如：抛出自定义异常或返回错误的 FriendshipAndContentDTO 对象
            return null;
        }

    }
    @GetMapping("/searchUsers")
    public List<SearchUser> searchUsers(@RequestParam("username") String username,int userId) {
        List<SearchUser> searchUsers = new ArrayList<>();

        // 根据用户名模糊匹配查询获取user_id列表
        List<Integer> userIdList = friendshipMapper.getUserIdsByUsername(username);

        for (Integer searchUserId : userIdList) {
            if (searchUserId != userId) {
                Map<Integer,String> map=new HashMap<>();
                String searchName=userMapper.selectUsernameById(searchUserId);
                map.put(searchUserId,searchName);
                Boolean isFriend = friendshipMapper.getFriendshipStatus(userId,searchUserId);
                if (isFriend==null){
                    isFriend=true;
                }
                // 组装SearchUser对象
                SearchUser searchUser = new SearchUser();
                searchUser.setUserId(searchUserId);
                searchUser.setUsername(searchName);
                searchUser.setStatus(isFriend); // true表示非好友，false表示好友关系
                searchUsers.add(searchUser);
            }
        }

        return searchUsers;
    }

    @PostMapping("/searchApply")
    public SearchUser isHaveApply(HttpServletRequest req){
        HttpSession session=req.getSession(false);
        SearchUser searchUser=new SearchUser();
        if (session == null){
            System.out.println("[isHaveApply] 获取会话失败！");
            return searchUser;
        }
        User user=(User) session.getAttribute("user");
        if (user == null){
            System.out.println("[isHaveApply] user==null ！");
            return searchUser;
        }
        Boolean result = friendshipMapper.isHaveApply(user.getUser_id());

        // 对查询结果进行判断
        if (result != null) {
            searchUser.setStatus(result);
        } else {
            // 处理查询结果为 null 的情况
            System.out.println("[isHaveApply] 查询结果为null！");
            // 设置默认值或做其他处理
        }

        return searchUser;
    }

    @GetMapping("/applyList")
    public List<FriendshipAndContentDTO> getAppliersList(int userId){
        List<FriendshipAndContentDTO> friendshipAndContentDTOS=friendshipMapper.getAppliersList(userId);
        System.out.println(friendshipAndContentDTOS);
        return friendshipAndContentDTOS;
    }
    @GetMapping("/getFriendId")
    public Friend getFriendId(String friendName){
        Friend friend=new Friend();
        Integer friendId = friendshipMapper.getFriendId(friendName);
        if (friendId>0){
            friend.setFriendId(friendId);
        }
        return friend;
    }

    @PostMapping("/isFriend")
    public boolean isFriend(int userId,int friendId){
        Boolean isFriend = friendshipMapper.getFriendshipStatus(userId,friendId);
        if (isFriend==null){
            return false;
        }
        if (!isFriend){
            return true;
        }
        return false;
    }

}


//      @GetMapping("/getFriendshipsByUserId")
//      public List<Friendship> getFriendshipsByUserId(int userId) {
//          return friendshipMapper.getFriendshipsByUserId(userId);
//      }


      // 其他处理好友相关的前端请求

